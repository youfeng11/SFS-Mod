package com.youfeng.sfsmod.data

import android.content.Context
import com.youfeng.sfsmod.utils.SignUtil
import com.youfeng.sfsmod.utils.copyAssetFile
import com.youfeng.sfsmod.utils.installApk
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import javax.inject.Inject
import javax.inject.Singleton

sealed class VerifySignatureStates {
    data object SignatureValid : VerifySignatureStates()
    data object SignatureMismatch : VerifySignatureStates()
    data object SignatureUnavailable : VerifySignatureStates()
}

@Singleton
class MainRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fileSystem = FileSystem.SYSTEM

    suspend fun copyResources(): VerifySignatureStates = withContext(Dispatchers.IO) {
        val dataPath = "${context.filesDir.parent}/shared_prefs/".toPath()
        val languagePath =
            context.getExternalFilesDir("Custom Translations")?.absolutePath?.toPath()
        val externalCachePath = context.externalCacheDir?.absolutePath?.toPath()

        listOfNotNull(dataPath, languagePath, externalCachePath).forEach {
            fileSystem.createDirectories(it)
        }

        context.copyAssetFile(
            "mod.xml",
            dataPath.resolve("com.StefMorojna.SpaceflightSimulator.v2.playerprefs.xml")
        )
        languagePath?.let { context.copyAssetFile("translation.txt", it.resolve("简体中文.txt")) }
        externalCachePath?.let { context.copyAssetFile("base.apk.1", it.resolve("temp.apk")) }

        return@withContext verifySignature(externalCachePath)
    }

    private fun verifySignature(externalCachePath: Path?): VerifySignatureStates {
        externalCachePath ?: return VerifySignatureStates.SignatureUnavailable
        val signUtil = SignUtil(context)
        val thisMD5 = signUtil.getCurrentAppSignatureMD5()
        val apkMD5 = signUtil.getApkSignatureMD5(externalCachePath.resolve("temp.apk").toString())
        return when {
            thisMD5.isNullOrEmpty() || apkMD5.isNullOrEmpty() -> VerifySignatureStates.SignatureUnavailable
            thisMD5 == apkMD5 -> VerifySignatureStates.SignatureValid
            else -> VerifySignatureStates.SignatureMismatch
        }
    }

    fun installApk() {
        context.externalCacheDir?.absolutePath?.toPath()?.resolve("temp.apk")
            ?.let { context.installApk(it) }
    }
}