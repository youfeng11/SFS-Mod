package com.youfeng.sfsmod.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.source

/**
 * 触发设备振动。
 * @param time 振动时长（毫秒），默认为250ms
 * 实现逻辑：
 * - API 26+（Android 8.0+）使用VibrationEffect实现精确控制
 * - API 31+（Android 12+）通过VibratorManager获取Vibrator实例
 * - 旧版本直接使用VIBRATOR_SERVICE
 */
fun Context.vibrate(time: Long = 250) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(time)
    }
}

/**
 * 从assets目录复制文件到指定路径（协程版）。
 * @param assetFileName assets目录中的文件名（如 "config.json"）
 * @param destinationPath 目标文件路径（使用Okio Path类型）
 * 注意：
 * - 使用Dispatchers.IO确保在IO线程执行
 * - 自动关闭输入输出流，避免资源泄漏
 */
suspend fun Context.copyAssetFile(assetFileName: String, destinationPath: Path) =
    withContext(Dispatchers.IO) {
        assets.open(assetFileName).source().buffer().use { src ->
            FileSystem.SYSTEM.sink(destinationPath).buffer().use { dst ->
                src.readAll(dst)
            }
        }
    }
