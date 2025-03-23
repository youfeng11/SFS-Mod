package com.youfeng.sfsmod.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youfeng.sfsmod.BuildConfig
import com.youfeng.sfsmod.R

/**
 * 关于对话框
 * @param htmlString 包含HTML链接的富文本
 * @param onDismissRequest 关闭对话框回调
 * 布局结构：
 * - 应用图标和版本信息
 * - 可选择的版本号文本
 * - 支持超链接的说明文本
 */
@Composable
fun AboutDialog(htmlString: String, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    // 应用图标显示
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp)) // 圆角裁剪
                    )
                    Spacer(Modifier.width(18.dp))

                    Column {
                        // 应用名称
                        Text(
                            stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )

                        // 可选中的版本号
                        SelectionContainer {
                            Text(
                                "v${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        // 富文本链接
                        AnnotatedLinkText(htmlString)
                    }
                }
            }
        }
    )
}

/**
 * HTML链接文本解析器
 * @param htmlString 包含<a>标签的HTML字符串
 * 特性：
 * - 链接显示为带下划线的主题色
 * - 按压状态有背景高亮
 * - 自动解析HTML实体
 */
@Composable
fun AnnotatedLinkText(htmlString: String) {
    val annotatedString = AnnotatedString.fromHtml(
        htmlString = htmlString,
        linkStyles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            ),
            pressedStyle = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.secondaryContainer,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
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

@Preview
@Composable
private fun AboutDialogPreview() {
    AboutDialog(stringResource(R.string.about_source_code)) { }
}
