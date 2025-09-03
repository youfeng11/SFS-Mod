package com.youfeng.sfsmod.ui.events

import okio.Path

/**
 * UI事件类型定义
 */
sealed class UiEvent {
    data class NavigateToInstall(val apkPath: Path) : UiEvent()
    data object Vibrate : UiEvent()
    data object RequestInstallPermission : UiEvent()
}