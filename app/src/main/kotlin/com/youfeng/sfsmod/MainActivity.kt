package com.youfeng.sfsmod

import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import java.util.Locale

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

import com.youfeng.sfsmod.BuildConfig
import com.youfeng.sfsmod.ui.screen.MainScreen
import com.youfeng.sfsmod.ui.theme.MainTheme
import com.youfeng.sfsmod.ui.viewmodel.MainViewModel
import com.youfeng.sfsmod.utils.SignUtil
import com.youfeng.sfsmod.utils.vibrate
import com.youfeng.sfsmod.utils.installApk
import com.youfeng.sfsmod.utils.copyAssetFile

class MainActivity : ComponentActivity() {

    private var coroutineScope: CoroutineScope? = null
    private val viewModel: MainViewModel by viewModels()

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

    // 复制应用所需的资源文件
    private suspend fun copyResources(): Int {
        val fileSystem = FileSystem.SYSTEM

        val dataPath = "${dataDir.absolutePath}/shared_prefs/".toPath()
        val languagePath = getExternalFilesDir("Custom Translations")?.absolutePath?.toPath()
        val externalCachePath = externalCacheDir?.absolutePath?.toPath()

        // 创建目录（如果不存在）
        fileSystem.createDirectories(dataPath)
        if (languagePath != null) fileSystem.createDirectories(languagePath)
        if (externalCachePath != null) fileSystem.createDirectories(externalCachePath)

        // 复制资源文件
        applicationContext.copyAssetFile("mod.xml", dataPath.resolve("com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml"))
        if (languagePath != null) {
            applicationContext.copyAssetFile("translation.txt", languagePath.resolve("简体中文.txt"))
        }
        if (externalCachePath != null) {
            applicationContext.copyAssetFile("base.apk.1", externalCachePath.resolve("temp.apk"))
        }

        return verifySignature(externalCachePath)
    }

    // 验证 APK 签名
    private fun verifySignature(externalCachePath: Path?): Int {
        if (externalCachePath == null) return 0
        val signUtil = SignUtil(applicationContext)
        val thisMD5 = signUtil.getCurrentAppSignatureMD5()
        val apkMD5 = signUtil.getApkSignatureMD5(externalCachePath.resolve("temp.apk").toString())
        return when {
            thisMD5.isNullOrEmpty() || apkMD5.isNullOrEmpty() -> 0
            thisMD5 == apkMD5 || BuildConfig.DEBUG -> 1
            else -> 2
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.state is MainViewModel.ScreenState.Loading || viewModel.state is MainViewModel.ScreenState.Done) {
            startCoroutine()
        }
    }

    fun startCoroutine() {
        viewModel.setLoadingState()
        coroutineScope = CoroutineScope(Dispatchers.Main + Job())
        coroutineScope?.launch {
            val result = copyResources()
            applicationContext.vibrate()
            when (result) {
                1 -> {
                    viewModel.setDoneState()
                    repeat(3) {
                        delay(1000)
                        viewModel.decrementTimer()
                    }
                    applicationContext.installApk(externalCacheDir?.absolutePath?.toPath()?.resolve("temp.apk") ?: return@launch)
                    finish()
                }
                2 -> viewModel.setErrorState(getString(R.string.error_sign))
                else -> viewModel.setErrorState(
                    getString(
                        R.string.error_none,
                        "${Build.BRAND} ${Build.MODEL} ${Build.DEVICE} ${Build.VERSION.SDK_INT}"
                    )
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopCoroutine()
    }

    fun stopCoroutine() {
        coroutineScope?.cancel()
        coroutineScope = null
    }
}