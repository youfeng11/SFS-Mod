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

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.startCoroutineOnStart()
                Lifecycle.Event.ON_STOP -> viewModel.stopCoroutine()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val context = LocalActivity.current
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MainViewModel.UiEvent.NavigateToApkInstall -> {
                    viewModel.repository.installApk()
                }
                is MainViewModel.UiEvent.Vibrate -> {
                    context?.vibrate()
                }
                is MainViewModel.UiEvent.Finish -> {
                    context?.finish()
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        MainLayout(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainLayout(viewModel: MainViewModel) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.topbar_title)) },
                actions = { OverflowMenu(viewModel) },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        ContentArea(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel
        )
    }
}

@Composable
private fun ContentArea(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        VersionInfo()
        LoadingSection(viewModel)
        Text(
            text = stringResource(R.string.warnning),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun VersionInfo() {
    val versionText = remember {
        "v${BuildConfig.VERSION_NAME.substringBefore("-")}（${BuildConfig.VERSION_CODE}）"
    }
    Text(text = versionText, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun LoadingSection(viewModel: MainViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = viewModel.state.collectAsState().value,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { state -> LoadingIcon(state) }

        Text(
            modifier = Modifier.animateContentSize(),
            textAlign = TextAlign.Center,
            text = when (viewModel.state.collectAsState().value) {
                is MainViewModel.ScreenState.Stopped -> stringResource(R.string.stopped)
                is MainViewModel.ScreenState.Done -> stringResource(
                    R.string.done,
                    viewModel.timer.collectAsState().value
                )

                is MainViewModel.ScreenState.Error -> when ((viewModel.state.collectAsState().value as MainViewModel.ScreenState.Error).errorType) {
                    is MainViewModel.ErrorType.SignatureMismatch -> stringResource(R.string.error_sign)
                    is MainViewModel.ErrorType.SignatureUnavailable -> stringResource(
                        R.string.error_none,
                        "${Build.BRAND}|${Build.MODEL}|${Build.DEVICE}|${Build.VERSION.SDK_INT}"
                    )
                }

                else -> stringResource(R.string.loading)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = when (viewModel.state.collectAsState().value) {
                is MainViewModel.ScreenState.Loading -> Color.Unspecified
                is MainViewModel.ScreenState.Error -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

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
    } ?: LinearProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}
