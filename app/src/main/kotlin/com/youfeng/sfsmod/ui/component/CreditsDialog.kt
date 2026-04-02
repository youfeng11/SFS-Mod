package com.youfeng.sfsmod.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.youfeng.sfsmod.R

/**
 * 开源许可信息对话框
 */
@Composable
fun CreditsDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 512.dp)
            .padding(horizontal = 36.dp),
        title = { Text(stringResource(R.string.osl)) },
        text = {
            val libraries by produceLibraries(R.raw.aboutlibraries)
            val scrollState = rememberLazyListState()

            LibrariesContainer(
                libraries = libraries,
                lazyListState = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalFadingEdge(
                        showTop = scrollState.canScrollBackward,
                        showBottom = scrollState.canScrollForward
                    ),
                colors = LibraryDefaults.libraryColors(
                    libraryBackgroundColor = Color.Transparent,
                    dialogBackgroundColor = MaterialTheme.colorScheme.background
                ),
                divider = {
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                },
                licenseDialogConfirmText = stringResource(R.string.ok)
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

/**
 * 为可滚动组件添加垂直方向的渐变边缘提示
 * @param showTop 是否显示顶部渐变（通常在可以向上滚动时显示）
 * @param showBottom 是否显示底部渐变（通常在可以向下滚动时显示）
 */
private fun Modifier.verticalFadingEdge(
    showTop: Boolean,
    showBottom: Boolean
) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadeHeight = 32.dp.toPx()

        // 只有在内容高度足以容纳渐变时才绘制
        if (size.height > fadeHeight * 2) {
            val brush = Brush.verticalGradient(
                0f to if (showTop) Color.Transparent else Color.Black,
                fadeHeight / size.height to Color.Black,
                1f - (fadeHeight / size.height) to Color.Black,
                1f to if (showBottom) Color.Transparent else Color.Black
            )
            drawRect(brush = brush, blendMode = BlendMode.DstIn)
        }
    }

@Preview
@Composable
private fun CreditsDialogPreview() {
    CreditsDialog {}
}
