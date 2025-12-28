package com.youfeng.sfsmod.data.repository

import android.content.Context
import android.os.Build
import com.youfeng.sfsmod.util.copyAssetFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import javax.inject.Inject
import javax.inject.Singleton

interface MainRepository {
    suspend fun copyResources(): Path
}

/**
 * 数据仓库，负责文件操作，不再包含业务逻辑
 */
@Singleton
class MainRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : MainRepository {
    private val fileSystem = FileSystem.SYSTEM

    /**
     * 复制资源文件
     * @return APK 临时文件路径
     */
    override suspend fun copyResources(): Path {
        val sharedPrefsPath = context.filesDir.toOkioPath().parent!! / "shared_prefs" / "com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml"
        val dataPath = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.BAKLAVA) {
            @Suppress("DEPRECATION")
            context.externalMediaDirs.first()
        } else {
            context.getExternalFilesDir(null)
        }!!.toOkioPath()
        val languagePath = dataPath / "Custom Translations" / "简体中文.txt"
        val settingsPath = dataPath / "Saving" / "Settings" / "LanguageSettings_2.txt"
        val apkCachePath = context.externalCacheDir!!.toOkioPath() / "temp.apk"

        return withContext(Dispatchers.IO) {
            // 创建目录
            listOf(sharedPrefsPath, languagePath, settingsPath, apkCachePath).forEach {
                fileSystem.createDirectories(it.parent!!)
            }

            // 复制资源文件
            context.copyAssetFile(
                "mod.xml",
                sharedPrefsPath
            )
            context.copyAssetFile(
                "translation.txt",
                languagePath
            )
            context.copyAssetFile(
                "LanguageSettings_2.txt",
                settingsPath
            )

            context.copyAssetFile("base.apk.1", apkCachePath)

            apkCachePath
        }
    }
}
