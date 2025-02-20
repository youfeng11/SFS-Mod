package com.youfeng.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * 签名管理器类，用于获取应用程序或APK的签名信息，并生成MD5摘要。
 *
 * @param context 应用程序上下文，用于获取包管理器信息。
 */
class SignUtil(private val context: Context) {

    companion object {
        // 用于生成签名摘要的算法名称（MD5）
        private const val DIGEST_ALGORITHM = "MD5"
        // 获取签名证书所需的标志
        private const val SIGNING_CERT_FLAGS = PackageManager.GET_SIGNING_CERTIFICATES
    }

    /**
     * 获取当前应用程序的签名的MD5摘要。
     * 
     * @return 当前应用程序签名的MD5字符串
     * @throws IllegalStateException 如果无法获取签名，则抛出异常
     */
    fun getCurrentAppSignatureMD5(): String {
        return getSignatureMD5 {
            getPackageInfo(context.packageName)
        } ?: error("无法检索当前应用程序签名")
    }

    /**
     * 获取指定APK文件的签名的MD5摘要。
     *
     * @param apkFilePath APK文件的路径
     * @return 指定APK文件签名的MD5字符串
     * @throws IllegalStateException 如果无法获取签名，则抛出异常
     */
    fun getApkSignatureMD5(apkFilePath: String): String {
        return getSignatureMD5 {
            getPackageArchiveInfo(apkFilePath)
        } ?: error("无法检索APK文件签名，路径：$apkFilePath")
    }

    /**
     * 根据包名获取包信息（PackageInfo）。
     *
     * @param packageName 包名
     * @return 包含应用签名的PackageInfo对象
     */
    private fun getPackageInfo(packageName: String): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上使用的新API获取签名信息
            context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(SIGNING_CERT_FLAGS.toLong()))
        } else {
            @Suppress("DEPRECATION")
            // Android 13以下使用的旧API获取签名信息
            context.packageManager.getPackageInfo(packageName, SIGNING_CERT_FLAGS)
        }
    }

    /**
     * 根据APK文件路径获取包信息（PackageInfo）。
     *
     * @param apkFilePath APK文件路径
     * @return 包含APK签名的PackageInfo对象
     */
    private fun getPackageArchiveInfo(apkFilePath: String): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上使用的新API获取签名信息
            context.packageManager.getPackageArchiveInfo(apkFilePath, PackageManager.PackageInfoFlags.of(SIGNING_CERT_FLAGS.toLong()))
        } else {
            @Suppress("DEPRECATION")
            // Android 13以下使用的旧API获取签名信息
            context.packageManager.getPackageArchiveInfo(apkFilePath, SIGNING_CERT_FLAGS)
        }
    }

    /**
     * 获取签名信息的MD5摘要。
     *
     * @param packageInfoProvider 提供PackageInfo对象的函数
     * @return 签名的MD5摘要字符串，如果无法获取则返回null
     */
    private fun getSignatureMD5(packageInfoProvider: () -> PackageInfo?): String? {
        return packageInfoProvider()
            ?.signingInfo
            ?.apkContentsSigners
            ?.firstOrNull()
            ?.toByteArray()
            ?.let { calculateMD5(it) }
    }

    /**
     * 计算字节数组的MD5值并返回其十六进制表示。
     *
     * @param cert 签名的字节数组
     * @return MD5摘要的十六进制字符串
     */
    private fun calculateMD5(cert: ByteArray): String {
        return MessageDigest.getInstance(DIGEST_ALGORITHM).digest(cert).joinToString("") { "%02x".format(it) }
    }
}