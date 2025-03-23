package com.youfeng.sfsmod.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
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
        title = { Text(stringResource(R.string.osl)) }, // R.string.osl对应"开源许可"
        text = {
            LibrariesContainer(
                modifier = Modifier.fillMaxSize() // 全屏高度显示库列表
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
