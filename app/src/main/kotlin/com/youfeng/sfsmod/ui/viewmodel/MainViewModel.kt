package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.data.repository.InstallPermissionRepository
import com.youfeng.sfsmod.data.repository.MainRepository
import com.youfeng.sfsmod.domain.state.VerifySignatureState
import com.youfeng.sfsmod.domain.usecase.VerifySignatureUseCase
import com.youfeng.sfsmod.ui.event.UiEvent
import com.youfeng.sfsmod.ui.state.AppState
import com.youfeng.sfsmod.ui.state.UiState
import com.youfeng.sfsmod.util.DeviceInfo
import com.youfeng.sfsmod.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Path
import javax.inject.Inject

/**
 * 主界面ViewModel，负责：
 * 1. 资源复制流程控制
 * 2. 签名验证状态管理
 * 3. 倒计时逻辑
 * 4. 系统事件触发（振动、导航等）
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val installPermissionRepository: InstallPermissionRepository,
    private val verifySignatureUseCase: VerifySignatureUseCase,
) : ViewModel() {

    // region 事件流配置
    /**
     * 一次性UI事件流（如导航、振动）
     */
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    // endregion

    // region 协程作用域
    /**
     * 主线程协程作用域
     */
    private var job: Job? = null
    // endregion

    // region 状态管理
    /**
     * 界面状态流，包含四种状态：
     * - Loading: 资源复制中
     * - Stopped: 流程被手动停止
     * - Done: 操作完成
     * - Error: 错误状态（含具体错误类型）
     */
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    // endregion

    // region 公共方法
    /**
     * 强制更新状态为已停止
     * 用于用户主动取消操作
     */
    fun menuStopOnClick() {
        stopCoroutine()
        _uiState.update { it.copy(appState = AppState.Stopped) }
    }

    /**
     * 安全启动协程的入口方法
     * 仅在Loading或Done状态允许重新启动
     */
    fun activityOnStart() {
        if (uiState.value.appState is AppState.Loading || uiState.value.appState is AppState.Done) {
            startCoroutine()
        }
    }

    /**
     * 启动核心业务协程
     * 执行流程：
     * 1. 取消已有协程
     * 2. 重置为Loading状态
     * 3. 启动资源复制协程
     */
    fun startCoroutine(skipPermissionCheck: Boolean = false) {
        stopCoroutine()
        // 检查权限

        job = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(appState = AppState.Loading) }
            if (!skipPermissionCheck && !installPermissionRepository.hasInstallPermission()) {
                _uiState.update { it.copy(showInstallPermissionDialog = true) }
                _uiEvent.send(UiEvent.Vibrate(150))
                return@launch
            }

            startResourceCopyProcess()
        }
    }

    /**
     * 停止所有子协程
     * 用于取消进行中的操作
     */
    fun stopCoroutine() {
        job?.cancel()
    }

    fun onSkipInstallPermission() {
        _uiState.update { it.copy(showInstallPermissionDialog = false) }
        startCoroutine(true)
    }

    fun onDismissInstallPermissionDialog() {
        _uiState.update { it.copy(showInstallPermissionDialog = false) }
        menuStopOnClick()
    }
    
    fun onConfirmInstallPermissionDialog() {
        requestInstallPermission()
    }

    /**
     * 当用户在对话框点击 "去授权" 时，由View层调用此方法
     */
    fun requestInstallPermission() {
        _uiEvent.trySend(UiEvent.RequestInstallPermission)
    }

    /**
     * 当用户从设置页返回时调用
     */
    fun onPermissionResult() {
        // 再次检查权限
        if (installPermissionRepository.hasInstallPermission()) {
            _uiState.update { it.copy(showInstallPermissionDialog = false) }
            startCoroutine()
        }
    }
    // endregion

    // region 私有逻辑
    /**
     * 处理资源复制结果
     *
     * 逻辑分支：
     * - 签名一致：启动倒计时并安装
     * - 签名不匹配：显示对应错误
     * - 签名获取失败：显示错误详细
     */
    private suspend fun handleCopyResult(result: VerifySignatureState, apkPath: Path) {
        if (result is VerifySignatureState.SignatureValid) {
            for (timer in 3 downTo 0) {
                _uiState.update { it.copy(appState = AppState.Done(timer)) }
                delay(1000)
            }
            _uiEvent.trySend(UiEvent.NavigateToInstall(apkPath))
        } else {
            val errMessage = when (result) {
                is VerifySignatureState.SignatureMismatch -> UiText.StringResource(R.string.error_signature_mismatch)
                is VerifySignatureState.SignatureUnavailableThis -> UiText.StringResource(R.string.error_null_this_signature)
                is VerifySignatureState.SignatureUnavailableApk -> UiText.StringResource(R.string.error_null_apk_signature)
                else -> UiText.StringResource(R.string.error_unknown, DeviceInfo.DeviceInfoString)
            }
            _uiState.update { it.copy(appState = AppState.Error(errMessage)) }
        }
    }

    private suspend fun startResourceCopyProcess() {
        try {
            val externalCachePath: Path = repository.copyResources()
            val result = verifySignatureUseCase(externalCachePath)
            _uiEvent.send(UiEvent.Vibrate()) // 操作完成触发振动反馈
            handleCopyResult(result, externalCachePath)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update { it.copy(appState = AppState.Error(UiText.StringResource(R.string.error_unknown, e.message))) }
        }
    }

    // endregion

}
