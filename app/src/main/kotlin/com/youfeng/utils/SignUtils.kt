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
        getSignatureMD5 { getPackageInfo(context.packageName) }
            ?: error("无法检索当前应用程序签名")

    /**
     * 获取指定 APK 文件的签名 MD5。
     */
    fun getApkSignatureMD5(apkFilePath: String): String =
        getSignatureMD5 { getPackageArchiveInfo(apkFilePath) }
            ?: error("无法检索 APK 文件签名，路径：$apkFilePath")

    /**
     * 根据包名获取 PackageInfo。
     */
    private fun getPackageInfo(packageName: String): PackageInfo? =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    packageName,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        PackageManager.GET_SIGNING_CERTIFICATES
                    else PackageManager.GET_SIGNATURES
                )
            }
        }.getOrNull()

    /**
     * 根据 APK 文件路径获取 PackageInfo。
     */
    private fun getPackageArchiveInfo(apkFilePath: String): PackageInfo? =
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageArchiveInfo(
                apkFilePath,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    PackageManager.GET_SIGNING_CERTIFICATES
                else PackageManager.GET_SIGNATURES
            )
        }.getOrNull()

    /**
     * 获取签名 MD5。
     */
    private fun getSignatureMD5(packageInfoProvider: () -> PackageInfo?): String? =
        packageInfoProvider()?.let { packageInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    packageInfo.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()?.md5()
            } else {
                    @Suppress("DEPRECATION")
                    packageInfo.signatures?.firstOrNull()?.toByteArray()?.md5()
            }
        }
}