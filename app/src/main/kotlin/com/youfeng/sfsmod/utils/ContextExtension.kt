package com.youfeng.sfsmod.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import android.os.Vibrator

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

import okio.sink
import okio.source
import okio.buffer
import okio.FileSystem
import okio.Path

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

suspend fun Context.copyAssetFile(assetFileName: String, destinationPath: Path) = withContext(Dispatchers.IO) {
        assets.open(assetFileName).source().buffer().use { src ->
            FileSystem.SYSTEM.sink(destinationPath).buffer().use { dst ->
                src.readAll(dst)
            }
        }
    }

