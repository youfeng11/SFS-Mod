package com.youfeng.sfsmod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.youfeng.sfsmod.ui.screen.MainScreen
import com.youfeng.sfsmod.ui.theme.MainTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Locale.getDefault().country != "CN") finish()
        setContent {
            MainTheme {
                MainScreen()
            }
        }
    }
}