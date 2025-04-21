package com.youfeng.sfsmod.data.repository

import android.content.Context
import com.youfeng.sfsmod.utils.copyAssetFile
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import javax.inject.Inject
import javax.inject.Singleton

interface Repository {
    suspend fun copyResources(): Path?
}

/**
 * 数据仓库，负责文件操作，不再包含业务逻辑
 */
@Singleton
class MainRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : Repository {
    private val fileSystem = FileSystem.SYSTEM

    /**
     * 复制资源文件
     * @return APK 临时文件路径
     */
    override suspend fun copyResources(): Path? {
        val dataPath = "${context.filesDir.parent}/shared_prefs/".toPath()
        val languagePath =
            context.getExternalFilesDir("Custom Translations")?.absolutePath?.toPath()
        val externalCachePath = context.externalCacheDir?.absolutePath?.toPath()

        // 创建目录
        listOfNotNull(dataPath, languagePath, externalCachePath).forEach {
            fileSystem.createDirectories(it)
        }

        // 复制资源文件
        context.copyAssetFile(
            "mod.xml",
            dataPath.resolve("com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml")
        )
        languagePath?.let { context.copyAssetFile("translation.txt", it.resolve("简体中文.txt")) }
        externalCachePath?.let { context.copyAssetFile("base.apk.1", it.resolve("temp.apk")) }

        return externalCachePath?.resolve("temp.apk")
    }

}
