package com.youfeng.sfsmod.ui.state

import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.util.UiText

data class UiState(
    val appState: AppState = AppState.Loading,
    val showInstallPermissionDialog: Boolean = false
)

/**
 * 界面状态密封类
 */
sealed class AppState {
    /** 资源复制中状态 */
    data object Loading : AppState()

    /** 用户主动停止状态 */
    data object Stopped : AppState()

    /** 操作成功完成状态 */
    data class Done(val timer: Int) : AppState()

    /** 错误状态（携带具体错误类型） */
    data class Error(val errorText: UiText = UiText.StringResource(R.string.error_unknown, "")) :
        AppState()
}
