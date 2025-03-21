package com.youfeng.sfsmod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.youfeng.sfsmod.ui.screen.MainScreen
import com.youfeng.sfsmod.ui.theme.MainTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Locale.getDefault().country != "CN") finish()
        setContent {
            MainTheme {
                MainScreen()
            }
        }
    }
}