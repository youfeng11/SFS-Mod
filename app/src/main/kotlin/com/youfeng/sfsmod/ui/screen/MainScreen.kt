package com.youfeng.sfsmod.ui.screen

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.youfeng.sfsmod.BuildConfig
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.ui.component.OverflowMenu
import com.youfeng.sfsmod.ui.viewmodel.MainViewModel
import com.youfeng.sfsmod.utils.vibrate

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
    val uiState by viewModel.state.collectAsState()
    val timer by viewModel.timer.collectAsState()


    // region 生命周期管理
    // 在 MainScreen 中使用
    LifecycleAwareHandler(
        onStart = viewModel::startCoroutineOnStart,
        onStop = viewModel::stopCoroutine
    )
    // endregion

    // region UI事件处理
    val context = LocalActivity.current
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MainViewModel.UiEvent.Finish -> {
                    context?.finish() // 关闭当前Activity
                }

                is MainViewModel.UiEvent.Vibrate -> {
                    context?.vibrate() // 短振动反馈
                }
            }
        }
    }
    // endregion

    // 基础布局容器
    Surface(modifier = Modifier.fillMaxSize()) {
        MainLayout(viewModel, uiState, timer)
    }
}

// 封装可复用的生命周期观察器
@Composable
fun LifecycleAwareHandler(
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_STOP -> onStop()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
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
private fun MainLayout(viewModel: MainViewModel, uiState: MainViewModel.ScreenState, timer: Int) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.topbar_title)) },
                actions = { OverflowMenu(viewModel, uiState) }, // 右上角菜单按钮
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing // 适配全面屏
    ) { innerPadding ->
        ContentArea(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            timer = timer
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
    uiState: MainViewModel.ScreenState,
    timer: Int
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        VersionInfo()
        LoadingSection(uiState, timer)
        Text(
            text = stringResource(R.string.warning),
            style = MaterialTheme.typography.bodyMedium
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
private fun LoadingSection(uiState: MainViewModel.ScreenState, timer: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 状态图标动画（淡入淡出300ms）
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { state -> LoadingIcon(state) }

        val errorTextBody = stringResource(
            R.string.error_none_body,
            "${Build.BRAND}|${Build.MODEL}|${Build.DEVICE}|${Build.VERSION.SDK_INT}"
        )
        // 动态文本（自动调整布局大小）
        Text(
            modifier = Modifier.animateContentSize(),
            textAlign = TextAlign.Center,
            text = when (uiState) {
                is MainViewModel.ScreenState.Stopped -> stringResource(R.string.stopped)
                is MainViewModel.ScreenState.Done -> stringResource(
                    R.string.done,
                    timer
                )

                is MainViewModel.ScreenState.Error -> when (uiState.errorType) {
                    is MainViewModel.ErrorType.SignatureMismatch -> stringResource(R.string.error_sign)

                    is MainViewModel.ErrorType.SignatureUnavailablePath -> "stringResource(R.string.error_none_path)}\n$errorTextBody"

                    is MainViewModel.ErrorType.SignatureUnavailableThis -> "${stringResource(R.string.error_none_this_signature)}\n$errorTextBody"

                    is MainViewModel.ErrorType.SignatureUnavailableApk -> "stringResource(R.string.error_none_apk_signature)}\n$errorTextBody"
                }

                else -> stringResource(R.string.loading)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = when (uiState) {
                is MainViewModel.ScreenState.Loading -> Color.Unspecified
                is MainViewModel.ScreenState.Error -> MaterialTheme.colorScheme.error // 错误状态显示红色
                else -> MaterialTheme.colorScheme.primary // 其他状态显示主题色
            }
        )
    }
}

/**
 * 状态指示图标组件
 * @param state 当前界面状态
 * 显示规则：
 * - 完成/停止/错误状态：显示对应图标
 * - 加载中：显示进度条
 */
@Composable
private fun LoadingIcon(state: MainViewModel.ScreenState) {
    val iconData = when (state) {
        is MainViewModel.ScreenState.Stopped -> Icons.Filled.Close to MaterialTheme.colorScheme.primary
        is MainViewModel.ScreenState.Done -> Icons.Filled.Done to MaterialTheme.colorScheme.primary
        is MainViewModel.ScreenState.Error -> Icons.Filled.Warning to MaterialTheme.colorScheme.error
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
