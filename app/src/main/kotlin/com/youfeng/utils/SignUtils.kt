package com.youfeng.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * 签名管理工具类，获取应用或 APK 的签名 MD5。
 */
class SignUtil(private val context: Context) {
    fun getCurrentAppSignatureMD5() : String? = getPackageSignatureMD5(context.packageName)

    fun getApkSignatureMD5(apkFilePath: String) : String? = getPackageSignatureMD5(apkFilePath, true)
    
    private fun getPackageSignatureMD5(target: String, isApk: Boolean = false): String? {
        fun getPackageInfo(target: String, flags: Int): PackageInfo? = if (isApk) {
                context.packageManager.getPackageArchiveInfo(target, flags)
            } else {
                context.packageManager.getPackageInfo(target, flags)
            }
        fun getPackageInfo(target: String, flags: PackageManager.PackageInfoFlags): PackageInfo? = if (isApk) {
                context.packageManager.getPackageArchiveInfo(target, flags)
            } else {
                context.packageManager.getPackageInfo(target, flags)
            }

        @Suppress("DEPRECATION")
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getPackageInfo(target, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()))?.signatureMD5()
        
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> getPackageInfo(target, PackageManager.GET_SIGNING_CERTIFICATES)?.signatureMD5() ?: getPackageInfo(target, PackageManager.GET_SIGNATURES)?.signatureMD5(true)
                    
            else -> getPackageInfo(target, PackageManager.GET_SIGNATURES)?.signatureMD5(true)
        }
    }
}
