package com.youfeng.sfsmod.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * 签名管理工具类，用于获取当前应用或指定APK文件的签名MD5值。
 * 兼容不同Android版本（处理了签名API的兼容性问题）。
 *
 * @property context 上下文对象，用于访问包管理服务。
 */
class SignUtil @Inject constructor(@ApplicationContext private val context: Context) {
    /**
     * 获取当前应用的签名MD5值。
     * @return 当前应用签名的MD5字符串，若获取失败返回null。
     */
    fun getCurrentAppSignatureMD5(): String? = getPackageSignatureMD5(context.packageName)

    /**
     * 获取指定APK文件的签名MD5值。
     * @param apkFilePath APK文件的完整路径。
     * @return APK签名的MD5字符串，若解析失败返回null。
     */
    fun getApkSignatureMD5(apkFilePath: String): String? = getPackageSignatureMD5(apkFilePath, true)

    /**
     * 核心方法：获取指定包名或APK路径的签名MD5。
     * @param target 当isApk=false时为包名，当isApk=true时为APK文件路径。
     * @param isApk 标识目标是否为APK文件，默认为false。
     * @return 签名MD5字符串，可能为null（如未找到签名或版本不兼容）。
     *
     * 实现逻辑：
     * 1. 根据Android版本选择不同的签名获取方式：
     *    - API 33+（Android 13+）使用PackageInfoFlags获取签名证书
     *    - API 28-32 优先尝试GET_SIGNING_CERTIFICATES，失败则降级到GET_SIGNATURES
     *    - API 28以下直接使用GET_SIGNATURES
     * 2. 使用扩展方法signatureMD5()将签名数据转换为MD5字符串
     */
    private fun getPackageSignatureMD5(target: String, isApk: Boolean = false): String? {
        // 内部方法：兼容旧版flags（Int类型）的PackageInfo获取
        fun getPackageInfo(target: String, flags: Int): PackageInfo? = if (isApk) {
            context.packageManager.getPackageArchiveInfo(target, flags)
        } else {
            context.packageManager.getPackageInfo(target, flags)
        }

        // 内部方法：适用于API 33+的新版flags（PackageInfoFlags类型）
        fun getPackageInfo(target: String, flags: PackageManager.PackageInfoFlags): PackageInfo? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (isApk) {
                    context.packageManager.getPackageArchiveInfo(target, flags)
                } else {
                    context.packageManager.getPackageInfo(target, flags)
                }
            } else null
        }

        @Suppress("DEPRECATION")
        return when {
            // Android 13+（API 33）使用新版PackageInfoFlags
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getPackageInfo(
                target,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )?.signatureMD5()

            // Android 9+（API 28-32）优先尝试新API，失败则降级到旧API
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> getPackageInfo(
                target,
                PackageManager.GET_SIGNING_CERTIFICATES
            )?.signatureMD5() ?: getPackageInfo(
                target,
                PackageManager.GET_SIGNATURES
            )?.signatureMD5(true)  // true表示使用旧版单签名模式

            // Android 8.1及以下直接使用旧版GET_SIGNATURES
            else -> getPackageInfo(target, PackageManager.GET_SIGNATURES)?.signatureMD5(true)
        }
    }
}
