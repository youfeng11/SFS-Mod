package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youfeng.sfsmod.data.model.VerifySignatureStates
import com.youfeng.sfsmod.data.repository.InstallPermissionRepository
import com.youfeng.sfsmod.data.repository.MainRepository
import com.youfeng.sfsmod.domain.usecase.CountdownUseCase
import com.youfeng.sfsmod.domain.usecase.VerifySignatureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
    private val countdownUseCase: CountdownUseCase
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
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val state: StateFlow<ScreenState> = _state

    // region 公共方法
    /**
     * 强制更新状态为已停止
     * 用于用户主动取消操作
     */
    fun menuStopOnClick() {
        stopCoroutine()
        sendState(ScreenState.Stopped)
    }

    /**
     * 安全启动协程的入口方法
     * 仅在Loading或Done状态允许重新启动
     */
    fun activityOnStart() {
        if (state.value is ScreenState.Loading || state.value is ScreenState.Done) {
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
    fun startCoroutine() {
        stopCoroutine()
        // 检查权限
        if (!installPermissionRepository.hasInstallPermission()) {
            sendState(ScreenState.PermissionRequired)
            return
        }

        sendState(ScreenState.Loading)
        job = viewModelScope.launch {
            val externalCachePath: Path? = repository.copyResources()
            val result = verifySignatureUseCase(externalCachePath)
            _uiEvent.send(UiEvent.Vibrate) // 操作完成触发振动反馈
            handleCopyResult(result, externalCachePath)
        }
    }

    /**
     * 停止所有子协程
     * 用于取消进行中的操作
     */
    fun stopCoroutine() {
        job?.cancel()
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
            startCoroutine()
        } else {
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
    private suspend fun handleCopyResult(result: VerifySignatureStates, apkPath: Path?) {
        if (result is VerifySignatureStates.SignatureValid) {

            countdownUseCase(
                onTick = { timer ->
                    sendState(ScreenState.Done(timer))
                },
                onFinish = {
                    if (apkPath != null) {
                        _uiEvent.trySend(UiEvent.NavigateToInstall(apkPath))
                    }
                }
            )
        } else {
            sendState(ScreenState.Error(result))
        }
    }

    private fun sendState(newState: ScreenState) {
        _state.update { newState }
    }

    // endregion

    override fun onCleared() {
        stopCoroutine()
        super.onCleared()
    }

}

/**
 * 界面状态密封类
 */
sealed class ScreenState {
    /** 资源复制中状态 */
    data object Loading : ScreenState()

    /** 用户主动停止状态 */
    data object Stopped : ScreenState()

    /** 未授予“安装未知应用”权限的暂停状态 */
    data object PermissionRequired : ScreenState()

    /** 操作成功完成状态 */
    data class Done(val timer: Int) : ScreenState()

    /** 错误状态（携带具体错误类型） */
    data class Error(val errorType: VerifySignatureStates) : ScreenState()
}

/**
 * UI事件类型定义
 */
sealed class UiEvent {
    data class NavigateToInstall(val apkPath: Path) : UiEvent()
    data object Vibrate : UiEvent()
    data object RequestInstallPermission : UiEvent()
}