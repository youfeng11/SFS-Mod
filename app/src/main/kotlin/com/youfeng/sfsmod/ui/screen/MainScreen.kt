package com.youfeng.sfsmod.ui.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youfeng.sfsmod.BuildConfig
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.ui.component.HighlightClickableText
import com.youfeng.sfsmod.ui.component.OverflowMenu
import com.youfeng.sfsmod.ui.event.UiEvent
import com.youfeng.sfsmod.ui.state.AppState
import com.youfeng.sfsmod.ui.state.UiState
import com.youfeng.sfsmod.ui.viewmodel.MainViewModel
import com.youfeng.sfsmod.util.DeviceInfo
import com.youfeng.sfsmod.util.UiText
import com.youfeng.sfsmod.util.installApk
import com.youfeng.sfsmod.util.vibrate

/**
 * 主界面入口，实现：
 * 1. 生命周期绑定（启动/停止协程）
 * 2. UI事件监听（振动、安装导航、退出）
 * 3. 整体布局容器
 *
 * @param viewModel 通过Hilt自动注入的ViewModel
 */
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    // region 生命周期管理
    // 在 MainScreen 中使用
    LifecycleAwareHandler(
        onStart = viewModel::activityOnStart,
        onStop = viewModel::stopCoroutine
    )
    // endregion

    // region UI事件处理
    val context = LocalActivity.current
    // 用于从系统设置页返回
    val installPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            // 当用户从设置页返回后，通知 ViewModel 刷新并继续流程
            viewModel.onPermissionResult()
        }
    )
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateToInstall -> {
                    context?.installApk(event.apkPath)
                    context?.finish()
                }

                is UiEvent.Vibrate -> context?.vibrate(event.time)

                // 跳转到系统设置页
                is UiEvent.RequestInstallPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            "package:${context?.packageName}".toUri()
                        )
                        installPermissionLauncher.launch(intent)
                    }
                }
            }
        }
    }
    // endregion

    // 3. 根据状态显示对话框
    if (uiState.showInstallPermissionDialog) {
        InstallPermissionDialog(
            onConfirm = { viewModel.onConfirmInstallPermissionDialog() },
            onDismiss = { viewModel.onDismissInstallPermissionDialog() },
            onSkip = { viewModel.onSkipInstallPermission() }
        )
    }

    // 基础布局容器
    Surface(modifier = Modifier.fillMaxSize()) {
        MainLayout(
            menuRestartOnClick = viewModel::startCoroutine,
            menuStopOnClick = viewModel::menuStopOnClick,
            uiState
        )
    }
}

/**
 * 引导用户开启安装权限的对话框
 */
@Composable
fun InstallPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.permission_dialog_title)) },
        text = {
            HighlightClickableText(
                text = stringResource(R.string.permission_dialog_message),
                modifier = Modifier.verticalScroll(rememberScrollState()),
                onClickHighlight = onSkip
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.permission_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.permission_dialog_cancel))
            }
        }
    )
}

// 封装可复用的生命周期观察器
@Composable
fun LifecycleAwareHandler(
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        onStart()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        onStop()
    }
}

/**
 * 主界面布局结构
 * 包含：
 * - 顶部应用栏（标题+菜单）
 * - 内容区域（版本信息、加载状态、警告文本）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainLayout(
    menuRestartOnClick: () -> Unit = {},
    menuStopOnClick: () -> Unit = {},
    uiState: UiState = UiState()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.topbar_title)) },
                actions = {
                    OverflowMenu(
                        menuRestartOnClick = menuRestartOnClick,
                        menuStopOnClick = menuStopOnClick,
                        uiState
                    )
                }, // 右上角菜单按钮
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        ContentArea(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState
        )
    }
}

/**
 * 内容区域组合
 * 垂直排列三个部分：
 * 1. 版本信息（顶部）
 * 2. 加载状态指示器（中部）
 * 3. 警告文本（底部）
 */
@Composable
private fun ContentArea(
    modifier: Modifier = Modifier,
    uiState: UiState
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        VersionInfo()
        LoadingSection(uiState)
        Text(
            text = stringResource(R.string.warning),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 显示应用版本信息
 * 格式示例："v2.1.0（210）"
 */
@Composable
private fun VersionInfo() {
    val versionText = remember {
        "v${BuildConfig.VERSION_NAME.substringBefore("-")}（${BuildConfig.VERSION_CODE}）"
    }
    Text(text = versionText, style = MaterialTheme.typography.titleMedium)
}

/**
 * 动态加载状态显示区域
 * 包含：
 * - 图标/进度条动画切换
 * - 状态文本描述
 */
@Composable
private fun LoadingSection(uiState: UiState, deviceInfo: String = DeviceInfo.DeviceInfoString) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val state = when (uiState.appState) {
            is AppState.Done -> AppState.Done(0)
            else -> uiState.appState
        }

        AnimatedContent(
            targetState = state
        ) { LoadingIcon(it) }

        AnimatedContent(
            targetState = state
        ) { appState ->
            Text(
                text = when (appState) {
                    is AppState.Stopped -> stringResource(R.string.stopped)

                    is AppState.Done -> stringResource(
                        R.string.done,
                        uiState.appState.let {
                            if (it is AppState.Done) it.timer else 0
                        }
                    )

                    is AppState.Error -> stringResource(
                        R.string.error_info,
                        appState.errorText.asString(),
                        deviceInfo
                    )

                    is AppState.Loading -> stringResource(R.string.loading)
                },
                modifier = Modifier.animateContentSize(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = when (appState) {
                    is AppState.Loading -> Color.Unspecified
                    is AppState.Error -> MaterialTheme.colorScheme.error // 错误状态显示红色
                    else -> MaterialTheme.colorScheme.primary // 其他状态显示主题色
                }
            )
        }
    }
}

/**
 * 状态指示图标组
 * 显示规则：
 * - 完成/停止/错误状态：显示对应图标
 * - 加载中：显示进度条
 */
@Composable
private fun LoadingIcon(uiState: AppState) {
    val iconData = when (uiState) {
        is AppState.Stopped -> Icons.Filled.Close to MaterialTheme.colorScheme.primary
        is AppState.Done -> Icons.Filled.Done to MaterialTheme.colorScheme.primary
        is AppState.Error -> Icons.Filled.Warning to MaterialTheme.colorScheme.error
        else -> null
    }

    iconData?.let { (icon, tint) ->
        Icon(
            icon,
            modifier = Modifier.size(48.dp),
            contentDescription = null,
            tint = tint
        )
    } ?: LinearProgressIndicator( // 加载中显示进度条
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}


// UI预览
@Preview(showBackground = true)
@Composable
fun MainLayoutLoadingPreview() {
    MainLayout(uiState = UiState())
}

@Preview(showBackground = true)
@Composable
fun MainLayoutStoppedPreview() {
    MainLayout(
        uiState = UiState(appState = AppState.Stopped)
    )
}

@Preview(showBackground = true)
@Composable
fun MainLayoutDonePreview() {
    MainLayout(
        uiState = UiState(appState = AppState.Done(3))
    )
}

@Preview(showBackground = true)
@Composable
fun MainLayoutErrorSignatureMismatchPreview() {
    MainLayout(
        uiState = UiState(
            appState = AppState.Error(UiText.DynamicString("Test"))
        )
    )
}
