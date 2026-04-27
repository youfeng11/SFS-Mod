package com.youfeng.sfsmod.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.ui.screen.MainScreen
import com.youfeng.sfsmod.ui.theme.MainTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        if (Locale.getDefault().country != "CN") {
            Log.e(
                getString(R.string.topbar_title),
                getString(R.string.not_supported_info, Locale.getDefault().country)
            )
            finish()
            return
        }

        setContent {
            MainTheme {
                MainScreen()
            }
        }
    }
}