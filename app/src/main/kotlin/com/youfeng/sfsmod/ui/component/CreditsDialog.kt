package com.youfeng.sfsmod.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mikepenz.aboutlibraries.ui.compose.DefaultChipColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.youfeng.sfsmod.R

/**
 * 开源许可信息对话框
 * @param onDismissRequest 关闭对话框的回调
 * 实现特性：
 * - 使用aboutlibraries库展示依赖项信息
 * - 自适应全屏高度
 */
@Composable
fun CreditsDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.widthIn(max = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() - 60.dp }),
        title = { Text(stringResource(R.string.osl)) }, // R.string.osl对应"开源许可"
        text = {
            LibrariesContainer(
                libraries = rememberLibraries().value,
                modifier = Modifier.fillMaxSize(), // 全屏高度显示库列表
                colors = LibraryDefaults.libraryColors(
                    backgroundColor = AlertDialogDefaults.containerColor,
                    licenseChipColors = DefaultChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = contentColorFor(MaterialTheme.colorScheme.primary),
                    )
                ),
                divider = {
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 8.dp
                            )
                    )
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close)) // 关闭按钮本地化文本
            }
        }
    )
}

/**
 * 对话框预览组件
 * 用于Android Studio的Compose预览功能
 */
@Preview
@Composable
private fun CreditsDialogPreview() {
    CreditsDialog {}
}
