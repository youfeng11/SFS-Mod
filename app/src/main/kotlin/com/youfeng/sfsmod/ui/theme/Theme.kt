package com.youfeng.sfsmod.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

// 通用的主题函数，应用全局主题
@Composable
fun MainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 检测系统主题是否为深色模式
    dynamicColor: Boolean = true, // 动态颜色支持（Android 12+）
    content: @Composable () -> Unit // 内容
) {
    // 根据设备的 API 版本和系统设置选择颜色方案
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80
        )

        else -> lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40
        )
    }
    // 设置状态栏颜色及其亮度
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced =
            false
    }

    // 使用 MaterialTheme 包裹 UI 内容
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}