package com.youfeng.sfsmod.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
//import androidx.compose.animation.scaleIn
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeOut
//import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyItemScope
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
//import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel

import com.youfeng.sfsmod.BuildConfig
import com.youfeng.sfsmod.MainActivity
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.ui.theme.MainTheme
import com.youfeng.sfsmod.ui.viewmodel.MainViewModel
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
fun MainScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        MainLayout()
    }
}

// 主界面布局
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: MainViewModel = viewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
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

// 主界面内容区
@Composable
fun ContentArea(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxSize()
    ) {
        VersionInfo()
        LoadingSection(viewModel)
        DisclaimerText()
    }
}

// 版本信息
@Composable
fun VersionInfo() {
    val versionText = remember {
        "v${BuildConfig.VERSION_NAME.substringBefore("-")}（${BuildConfig.VERSION_CODE}）"
    }
    Text(
        text = versionText,
        style = MaterialTheme.typography.titleMedium
    )
}

// 加载进度条
@Composable
fun LoadingSection(viewModel: MainViewModel) {
    // 状态文本切换动画
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
    AnimatedContent(
        targetState = viewModel.state,
        transitionSpec = {
        fadeIn(
            animationSpec = tween(300)
        ) togetherWith fadeOut(animationSpec = tween(300))
    }
    ) { state ->
        when (state) {
            0 -> {LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )}
            1 -> {Icon(
            Icons.Filled.Close,
            modifier = Modifier.size(width = 48.dp, height = 48.dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )}
            2 -> {Icon(
            Icons.Filled.Done,
            modifier = Modifier.size(width = 48.dp, height = 48.dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )}
            else -> Unit
        }
    }
    Text(
            modifier = Modifier.animateContentSize(),
            text = when (viewModel.state) {
            1 -> stringResource(R.string.stopped)
            2 -> stringResource(R.string.done, viewModel.timer)//"完成，${viewModel.timer}秒后开始安装"
            else -> stringResource(R.string.loading)
        },
            style = MaterialTheme.typography.bodyLarge,
            color = if (viewModel.state == 0) Color.Unspecified else MaterialTheme.colorScheme.primary
        )
    }
}

// 免责声明
@Composable
fun DisclaimerText() {
    Text(
        text = stringResource(R.string.warnning),
        style = MaterialTheme.typography.bodyMedium
    )
}

// 溢出菜单
@Composable
private fun OverflowMenu(viewModel: MainViewModel) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var openAboutDialog by rememberSaveable { mutableStateOf(false) }
    var openCreditsDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current as? MainActivity

    IconButton(onClick = { menuExpanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_vert))
    }

    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
        val (menuText, menuIcon) = if (viewModel.state == 1) {
            stringResource(R.string.menu_refresh) to Icons.Filled.Refresh
        } else {
            stringResource(R.string.menu_stop) to Icons.Filled.Close
        }

        MenuItem(menuText, menuIcon) {
            menuExpanded = false
            context?.let {
                it.StopCoroutine()
                if (viewModel.state != 1)  viewModel.stoppedState() else it.StartCoroutine()
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

    if (openAboutDialog) {
        AboutDialog { openAboutDialog = false }
    }
    if (openCreditsDialog) {
        CreditsDialog { openCreditsDialog = false }
    }
}

// 菜单项封装
@Composable
fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick
    )
}

// 关于对话框
@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text(
                            stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                        SelectionContainer {
                        Text(
                                "v${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        AnnotatedLinkText(R.string.about_source_code)
                    }
                }
            }
        }
    )
}

// 富文本链接
@Composable
fun AnnotatedLinkText(resId: Int) {
    val annotatedString = AnnotatedString.fromHtml(
        htmlString = stringResource(id = R.string.about_source_code),
        linkStyles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold // 设置粗体
            ),
            pressedStyle = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.secondaryContainer,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold // 设置粗体
            )
        )
    )
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp
    )
}

// 开源库对话框
@Composable
fun CreditsDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.osl)) },
        text = { LibrariesContainer(modifier = Modifier.fillMaxSize()) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.close)) }
        }
    )
}