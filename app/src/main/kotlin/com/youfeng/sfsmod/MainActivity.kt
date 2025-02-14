package com.youfeng.sfsmod

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.os.Vibrator
import android.net.Uri
import android.view.View
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

import com.youfeng.sfsmod.ui.screen.MainScreen
import com.youfeng.sfsmod.ui.theme.MainTheme
import com.youfeng.sfsmod.ui.viewmodel.MainViewModel
import com.youfeng.utils.SignUtil

class MainActivity : ComponentActivity() {

    private val enableSignVerification = true // 是否启用签名冲突验证(仅release)
    private var coroutineScope: CoroutineScope? = null // 协程Scope, 处理生命周期管理
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        if (Locale.getDefault().getCountry()!="CN") finish()
        setContent {
            MainTheme {
                MainScreen()
            }
        }
    }

    // 复制 assets 文件到指定目录，使用协程确保在后台执行，避免阻塞主线程
    private suspend fun copyAssetFile(assetFileName: String, destinationFile: File) = withContext(Dispatchers.IO) {
        assets.open(assetFileName).use { inputStream ->
            FileOutputStream(destinationFile).use { output ->
                // 使用 8KB 的缓冲区提升文件复制性能
                inputStream.copyTo(output, 8 * 1024)
            }
        }
    }

    // 复制应用所需的资源文件，包括破解补丁、语言包等
    private suspend fun copyResources(): Boolean {
        // 定义资源文件路径
        val dataPath = File("${dataDir.absolutePath}/shared_prefs/")
        val languagePath = File(getExternalFilesDir("Custom Translations").toString())
        val externalCachePath = File(externalCacheDir.toString())

        // 确保路径存在，避免潜在的崩溃
        listOf(dataPath, languagePath, externalCachePath).forEach { it.mkdirs() }

        // 依次复制资源文件
        copyAssetFile("mod.xml", File(dataPath, "com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml"))
        copyAssetFile("translation.txt", File(languagePath, "简体中文.txt"))
        copyAssetFile("base", File(externalCachePath, "temp.apk"))

        // 验证签名是否一致，避免 APK 签名冲突
        return verifySignature(externalCachePath)
    }

    // 验证 APK 签名是否与当前应用签名一致
    private fun verifySignature(externalCachePath: File): Boolean {
        val signUtil = SignUtil(applicationContext)
        return if (!BuildConfig.DEBUG && enableSignVerification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 对比当前应用签名与解压的 APK 文件签名
            signUtil.getCurrentAppSignatureMD5() != signUtil.getApkSignatureMD5(File(externalCachePath, "temp.apk").toString())
        } else false
    }

    // 安装 APK 文件，适配不同 Android 版本的文件 URI 访问方式
    private fun installApk(apkFile: File) {
        // 获取文件 URI，根据 Android 版本选择获取方式
        val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "$packageName.provider", apkFile)
        } else {
            Uri.fromFile(apkFile)
        }
        // 启动安装 APK 的 Intent
        Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(apkUri, "application/vnd.android.package-archive")
        }.also {
            startActivity(it)
        }
    }

    // 获取设备振动器的实例，适配不同 Android 版本的实现方式
    private fun getVibrator(): Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // 触发设备振动，适配不同 Android 版本的振动 API
    private fun vibrate() {
        val vibrator = getVibrator()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(250)
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.state != 1) {
            StartCoroutine()
        }
    }
    
    fun StartCoroutine() {
        viewModel.loadingState()
        // 显示加载动画并隐藏完成图标
        /*binding.apply {
            wait.visibility = View.VISIBLE
            done.visibility = View.INVISIBLE
        }*/

        // 启动协程执行异步任务
        coroutineScope = CoroutineScope(Dispatchers.Main + Job())
        coroutineScope?.launch {
            val result = copyResources() // 异步复制资源文件
            /*// 隐藏加载动画，显示完成图标和动画
            binding.apply {
            wait.visibility = View.INVISIBLE
                done.visibility = View.VISIBLE
                done.setImageDrawable(
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.anim_done_mark)
                )
                (done.drawable as? AnimatedVectorDrawable)?.start()
            }*/
            vibrate() // 触发设备震动反馈
            //showSnackbar(result) // 显示签名校验结果的提示信息
            viewModel.doneState()
            // 根据签名校验结果延迟一段时间再处理
            delay(3000)
            if (!result) installApk(File(externalCacheDir, "temp.apk")) // 安装 APK 文件
            finish() // 任务完成后关闭 Activity
        }
    }

    override fun onStop() {
        super.onStop()
        StopCoroutine()
    }
        
    fun StopCoroutine() {
        coroutineScope?.cancel() // 停止协程，取消正在运行的任务
        coroutineScope = null // 清空协程Scope引用
    }
}
