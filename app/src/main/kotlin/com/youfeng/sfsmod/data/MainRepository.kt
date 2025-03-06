package com.youfeng.sfsmod.data

import android.content.Context
import android.os.Build

import com.youfeng.sfsmod.BuildConfig
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.utils.SignUtil
import com.youfeng.sfsmod.utils.copyAssetFile
import com.youfeng.sfsmod.utils.installApk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class MainRepository(private val context: Context) {

    private val fileSystem = FileSystem.SYSTEM

    suspend fun copyResources(): Int = withContext(Dispatchers.IO) {
        val dataPath = "${context.dataDir.absolutePath}/shared_prefs/".toPath()
        val languagePath = context.getExternalFilesDir("Custom Translations")?.absolutePath?.toPath()
        val externalCachePath = context.externalCacheDir?.absolutePath?.toPath()

        listOfNotNull(dataPath, languagePath, externalCachePath).forEach {
            fileSystem.createDirectories(it)
        }

        context.copyAssetFile("mod.xml", dataPath.resolve("com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml"))
        languagePath?.let { context.copyAssetFile("translation.txt", it.resolve("简体中文.txt")) }
        externalCachePath?.let { context.copyAssetFile("base.apk.1", it.resolve("temp.apk")) }

        return@withContext verifySignature(externalCachePath)
    }

    private fun verifySignature(externalCachePath: Path?): Int {
        if (BuildConfig.DEBUG) return 1
        externalCachePath ?: return 0
        val signUtil = SignUtil(context)
        val thisMD5 = signUtil.getCurrentAppSignatureMD5()
        val apkMD5 = signUtil.getApkSignatureMD5(externalCachePath.resolve("temp.apk").toString())
        return when {
            thisMD5.isNullOrEmpty() || apkMD5.isNullOrEmpty() -> 0
            thisMD5 == apkMD5 -> 1
            else -> 2
        }
    }

    fun installApk() {
        context.externalCacheDir?.absolutePath?.toPath()?.resolve("temp.apk")?.let { context.installApk(it) }
    }
}