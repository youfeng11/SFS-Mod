package com.youfeng.sfsmod.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any // 保持 Any，但我们在解析时特殊处理 UiText
    ) : UiText()

    // 核心改动：在 Composable 中使用的解析函数
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                // 递归解析参数：如果参数是 UiText，则先递归调用 asString() 解析它
                val resolvedArgs = args.map { arg ->
                    if (arg is UiText) arg.asString() else arg
                }.toTypedArray()

                stringResource(resId, *resolvedArgs)
            }
        }
    }

    // 可以在非 Composable 环境中使用 (例如用于 log 或 Toast)
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                // 递归解析参数
                val resolvedArgs = args.map { arg ->
                    if (arg is UiText) arg.asString(context) else arg
                }.toTypedArray()

                context.getString(resId, *resolvedArgs)
            }
        }
    }
}