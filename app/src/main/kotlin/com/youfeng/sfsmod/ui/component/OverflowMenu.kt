package com.youfeng.sfsmod.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.youfeng.sfsmod.R

/**
 * 右上角溢出菜单组件
 * 功能特性：
 * - 动态切换"停止/刷新"按钮状态
 * - 显示关于和开源许可对话框
 * - 300ms淡入淡出动效
 */
@Composable
fun OverflowMenu(
    startCoroutine: () -> Unit,
    stopCoroutine: () -> Unit,
    setStoppedState: () -> Unit,
    uiState: MainViewModel.ScreenState
) {
    // region 状态管理
    var menuExpanded by remember { mutableStateOf(false) }
    var openAboutDialog by remember { mutableStateOf(false) }
    var openCreditsDialog by remember { mutableStateOf(false) }
    // endregion

    // 菜单触发按钮
    IconButton(onClick = { menuExpanded = true }) {
        Icon(
            Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more_vert) // 无障碍描述
        )
    }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        // 动态切换的操作项
        AnimatedContent(
            targetState = uiState is MainViewModel.ScreenState.Loading
                    || uiState is MainViewModel.ScreenState.Done,
        ) { isRunning ->
            MenuItem(
                text = stringResource(if (isRunning) R.string.menu_stop else R.string.menu_refresh),
                icon = if (isRunning) Icons.Filled.Close else Icons.Filled.Refresh
            ) {
                menuExpanded = false
                stopCoroutine()
                if (isRunning) setStoppedState() else startCoroutine()
            }
        }

        HorizontalDivider() // 菜单项分隔线

        // 静态菜单项
        MenuItem(stringResource(R.string.menu_about), Icons.Outlined.Info) {
            menuExpanded = false
            openAboutDialog = true
        }

        MenuItem(stringResource(R.string.menu_credits), Icons.Outlined.ContactPage) {
            menuExpanded = false
            openCreditsDialog = true
        }
    }

    // 对话框控制
    if (openAboutDialog) AboutDialog(stringResource(R.string.about_source_code)) {
        openAboutDialog = false
    }
    if (openCreditsDialog) CreditsDialog { openCreditsDialog = false }
}

/**
 * 通用菜单项组件
 * @param text 菜单项显示文本
 * @param icon 左侧图标资源
 * @param onClick 点击回调
 */
@Composable
private fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick
    )
}
