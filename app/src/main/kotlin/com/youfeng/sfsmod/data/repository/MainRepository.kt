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
        val dataPath = context.filesDir.toOkioPath().parent!!.resolve("shared_prefs")
        val languagePath = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.BAKLAVA) {
            @Suppress("DEPRECATION")
            context.externalMediaDirs!!.first().toOkioPath().resolve("Custom Translations")
        } else {
            context.getExternalFilesDir("Custom Translations")!!.toOkioPath()
        }
        val externalCachePath = context.externalCacheDir!!.toOkioPath()

        return withContext(Dispatchers.IO) {
            // 创建目录
            listOf(dataPath, languagePath, externalCachePath).forEach {
                fileSystem.createDirectories(it)
            }

            // 复制资源文件
            context.copyAssetFile(
                "mod.xml",
                dataPath.resolve("com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml")
            )
            context.copyAssetFile(
                "translation.txt",
                languagePath.resolve("简体中文.txt")
            )

            val apkPath = externalCachePath.resolve("temp.apk")
            context.copyAssetFile("base.apk.1", apkPath)

            apkPath
        }
    }

}
