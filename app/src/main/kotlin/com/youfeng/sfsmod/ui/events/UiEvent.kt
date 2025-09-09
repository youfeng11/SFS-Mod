package com.youfeng.sfsmod.ui.events

import okio.Path

/**
 * UI事件类型定义
 */
sealed class UiEvent {
    data class NavigateToInstall(val apkPath: Path) : UiEvent()
    data class Vibrate(val time: Long = 250) : UiEvent()
    data object RequestInstallPermission : UiEvent()
}