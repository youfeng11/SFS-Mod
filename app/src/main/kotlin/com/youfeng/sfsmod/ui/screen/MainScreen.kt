package com.youfeng.sfsmod.ui.screen

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.youfeng.sfsmod.ui.component.AboutDialog
import com.youfeng.sfsmod.ui.component.CreditsDialog
import com.youfeng.sfsmod.ui.viewmodel.MainViewModel

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
    LaunchedEffect(Unit) {
        viewModel.finishEvent.collect {
            context?.finish()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        MainLayout(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: MainViewModel) {
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
fun ContentArea(modifier: Modifier = Modifier, viewModel: MainViewModel) {
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
fun VersionInfo() {
    val versionText = remember {
        "v${BuildConfig.VERSION_NAME.substringBefore("-")}（${BuildConfig.VERSION_CODE}）"
    }
    Text(text = versionText, style = MaterialTheme.typography.titleMedium)
}

@Composable
fun LoadingSection(viewModel: MainViewModel) {
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

                is MainViewModel.ScreenState.Error -> (viewModel.state.collectAsState().value as MainViewModel.ScreenState.Error).message
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
fun LoadingIcon(state: MainViewModel.ScreenState) {
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

@Composable
private fun OverflowMenu(viewModel: MainViewModel) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var openAboutDialog by rememberSaveable { mutableStateOf(false) }
    var openCreditsDialog by rememberSaveable { mutableStateOf(false) }

    IconButton(onClick = { menuExpanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_vert))
    }

    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
        AnimatedContent(
            targetState = viewModel.state.collectAsState().value is MainViewModel.ScreenState.Loading || viewModel.state.collectAsState().value is MainViewModel.ScreenState.Done,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { isRunning ->
            MenuItem(
                text = stringResource(if (isRunning) R.string.menu_stop else R.string.menu_refresh),
                icon = if (isRunning) Icons.Filled.Close else Icons.Filled.Refresh
            ) {
                menuExpanded = false
                viewModel.stopCoroutine()
                if (isRunning) viewModel.setStoppedState() else viewModel.startCoroutine()
            }
        }

        HorizontalDivider()

        MenuItem(stringResource(R.string.menu_about), Icons.Outlined.Info) {
            menuExpanded = false
            openAboutDialog = true
        }

        MenuItem(stringResource(R.string.menu_credits), Icons.Outlined.ContactPage) {
            menuExpanded = false
            openCreditsDialog = true
        }
    }

    if (openAboutDialog) AboutDialog(stringResource(R.string.about_source_code)) {
        openAboutDialog = false
    }
    if (openCreditsDialog) CreditsDialog { openCreditsDialog = false }
}

@Composable
fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick
    )
}
