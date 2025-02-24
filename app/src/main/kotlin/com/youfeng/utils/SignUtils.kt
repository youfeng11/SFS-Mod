package com.youfeng.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.youfeng.utils.md5

/**
 * 签名管理工具类，获取应用或 APK 的签名 MD5。
 */
class SignUtil(private val context: Context) {

    /**
     * 获取当前应用程序的签名 MD5。
     */
    fun getCurrentAppSignatureMD5(): String =
        requireNotNull(getSignatureMD5(context.packageName)) { "无法检索当前应用程序签名" }

    /**
     * 获取指定 APK 文件的签名 MD5。
     */
    fun getApkSignatureMD5(apkFilePath: String): String =
        requireNotNull(getSignatureMD5(apkFilePath, isApk = true)) { "无法检索 APK 文件签名，路径：$apkFilePath" }

    /**
     * 获取签名 MD5，支持应用包名和 APK 文件路径。
     */
    private fun getSignatureMD5(identifier: String, isApk: Boolean = false): String? =
        getPackageInfo(identifier, isApk)?.let { packageInfo ->
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ->
                    packageInfo.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()?.md5()
                        ?: packageInfo.signatures?.firstOrNull()?.toByteArray()?.md5()

                else -> @Suppress("DEPRECATION")
                packageInfo.signatures?.firstOrNull()?.toByteArray()?.md5()
            }
        }

    /**
     * 获取 `PackageInfo`，可用于获取应用包或 APK 文件信息。
     */
    private fun getPackageInfo(identifier: String, isApk: Boolean): PackageInfo? =
        runCatching {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                PackageManager.GET_SIGNING_CERTIFICATES
            else PackageManager.GET_SIGNATURES

            if (isApk) {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageArchiveInfo(identifier, flags)
                    ?: context.packageManager.getPackageArchiveInfo(identifier, PackageManager.GET_SIGNATURES)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        identifier,
                        PackageManager.PackageInfoFlags.of(flags.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(identifier, flags)
                }
            }
        }.getOrNull()
}