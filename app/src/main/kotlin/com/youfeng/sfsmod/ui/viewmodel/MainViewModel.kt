package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.youfeng.sfsmod.data.VerifySignatureStates
import com.youfeng.sfsmod.domain.usecase.CopyResourcesUseCase
import com.youfeng.sfsmod.domain.usecase.InstallApkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主界面ViewModel，负责：
 * 1. 资源复制流程控制
 * 2. 签名验证状态管理
 * 3. 倒计时逻辑
 * 4. 系统事件触发（振动、导航等）
 *
 * @constructor 通过Hilt注入依赖
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val copyResourcesUseCase: CopyResourcesUseCase,
    private val installApkUseCase: InstallApkUseCase
) : ViewModel() {

    // region 事件流配置
    /**
     * 一次性UI事件流（如导航、振动）
     * 使用SharedFlow避免重复消费问题
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()
    // endregion

    // region 协程作用域
    /**
     * 主线程协程作用域，使用SupervisorJob实现子协程独立失败
     * 注意：Dispatchers.Main.immediate在UI操作中更高效
     */
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
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

    /**
     * 倒计时状态流（单位：秒）
     * 从3开始倒计时到0后触发导航
     */
    private val _timer = MutableStateFlow(3)
    val timer: StateFlow<Int> = _timer
    // endregion

    // region 公共方法
    /**
     * 强制更新状态为已停止
     * 用于用户主动取消操作
     */
    fun setStoppedState() {
        _state.update { ScreenState.Stopped }
    }

    /**
     * 安全启动协程的入口方法
     * 仅在Loading或Done状态允许重新启动
     */
    fun startCoroutineOnStart() {
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

        _state.update { ScreenState.Loading }
        coroutineScope.launch {
            val result = copyResourcesUseCase()
            _uiEvent.emit(UiEvent.Vibrate) // 操作完成触发振动反馈
            handleCopyResult(result)
        }
    }

    /**
     * 停止所有子协程
     * 用于取消进行中的操作
     */
    fun stopCoroutine() {
        coroutineScope.coroutineContext.cancelChildren()
    }
    // endregion

    // region 私有逻辑
    /**
     * 处理资源复制结果
     * @param result 来自Repository的验证结果
     *
     * 逻辑分支：
     * - 签名有效：启动倒计时并导航
     * - 签名不匹配：显示对应错误
     * - 签名不可用：显示系统级错误
     */
    private suspend fun handleCopyResult(result: VerifySignatureStates) {
        when (result) {
            is VerifySignatureStates.SignatureValid -> {
                _state.update { ScreenState.Done }
                // 3秒倒计时逻辑
                for (i in 3 downTo 0) {
                    _timer.update { i }
                    delay(1000)
                }
                installApkUseCase()
                _uiEvent.emit(UiEvent.Finish)
            }

            is VerifySignatureStates.SignatureMismatch -> _state.update {
                ScreenState.Error(
                    ErrorType.SignatureMismatch
                )
            }

            is VerifySignatureStates.SignatureUnavailable -> _state.update {
                ScreenState.Error(
                    ErrorType.SignatureUnavailable
                )
            }
        }
    }
    // endregion

    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }

    // region 状态定义
    /**
     * 界面状态密封类
     */
    sealed class ScreenState {
        /** 资源复制中状态 */
        data object Loading : ScreenState()

        /** 用户主动停止状态 */
        data object Stopped : ScreenState()

        /** 操作成功完成状态 */
        data object Done : ScreenState()

        /** 错误状态（携带具体错误类型） */
        data class Error(val errorType: ErrorType) : ScreenState()
    }

    /**
     * 错误类型枚举
     */
    sealed class ErrorType {
        /** 签名不匹配（APK被篡改） */
        data object SignatureMismatch : ErrorType()

        /** 无法获取签名（系统限制） */
        data object SignatureUnavailable : ErrorType()
    }

    /**
     * UI事件类型定义
     */
    sealed class UiEvent {
        /** 跳转至APK安装界面 */
        data object Finish : UiEvent()

        /** 触发设备振动反馈 */
        data object Vibrate : UiEvent()
    }
    // endregion
}
