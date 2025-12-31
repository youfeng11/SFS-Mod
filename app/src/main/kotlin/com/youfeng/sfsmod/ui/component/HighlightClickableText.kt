package com.youfeng.sfsmod.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

@Composable
fun HighlightClickableText(
    text: String,
    modifier: Modifier = Modifier,
    onClickHighlight: () -> Unit
) {
    val annotated = buildAnnotatedString {
        var currentIndex = 0
        while (currentIndex < text.length) {
            val start = text.indexOf("**", currentIndex)
            if (start == -1) {
                // 没有更多 **，直接添加剩余文本
                append(text.substring(currentIndex))
                break
            }
            // 添加前面的普通文本
            append(text.substring(currentIndex, start))

            val end = text.indexOf("**", start + 2)
            if (end == -1) {
                // 没有闭合，直接添加剩余
                append(text.substring(start))
                break
            }

            val clickableText = text.substring(start + 2, end)

            // 添加可点击部分
            withLink(
                LinkAnnotation.Clickable(
                    tag = "highlight",
                    styles = TextLinkStyles(
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
                    ),
                    linkInteractionListener = { _ -> onClickHighlight() }
                )
            ) {
                append(clickableText)
            }

            currentIndex = end + 2
        }
    }

    Text(text = annotated, modifier = modifier)
}