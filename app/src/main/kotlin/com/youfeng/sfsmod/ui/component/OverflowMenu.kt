package com.youfeng.sfsmod.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.ui.viewmodel.MainViewModel

@Composable
fun OverflowMenu(viewModel: MainViewModel) {
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
                viewModel.apply {
                    stopCoroutine()
                    if (isRunning) setStoppedState() else startCoroutine()
                }
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
private fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick
    )
}
