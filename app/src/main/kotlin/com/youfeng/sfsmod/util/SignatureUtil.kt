package com.youfeng.sfsmod.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.Path
import javax.inject.Inject

/**
 * 应用签名工具类，提供获取当前应用及指定APK文件签名信息的功能
 *
 * 通过依赖注入获取应用上下文，适配不同Android版本签名获取方式
 * @property context 应用上下文，用于访问包管理服务
 */
class SignatureUtil @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /**
     * 获取当前应用的第一个签名证书
     * @return 当前应用的签名信息，若获取失败返回null
     */
    fun getCurrentAppSignature(): Signature? {
        return getSignature(context.packageName) { name, flags ->
            context.packageManager.getPackageInfo(name, flags)
        }
    }

    /**
     * 获取指定APK文件的第一个签名证书
     * @param apkFilePath APK文件路径
     * @return APK文件的签名信息，若获取失败返回null
     */
    fun getApkSignature(apkFilePath: Path): Signature? {
        return getSignature(apkFilePath.toString()) { path, flags ->
            context.packageManager.getPackageArchiveInfo(path, flags)
        }
    }

    /**
     * 统一签名获取方法
     * @param packageNameOrPath 包名（已安装应用）或APK文件路径
     * @param getPackageInfo 获取包信息的函数（区分已安装应用/APK文件）
     */
    private inline fun getSignature(
        packageNameOrPath: String,
        crossinline getPackageInfo: (String, Int) -> PackageInfo?
    ): Signature? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28+ 使用新版签名证书获取方式
            getPackageInfo(packageNameOrPath, PackageManager.GET_SIGNING_CERTIFICATES)
                ?.signingInfo
                ?.apkContentsSigners
                ?.firstOrNull()
                ?: if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // 新版获取失败时回退旧版方式
                    @Suppress("DEPRECATION")
                    getPackageInfo(packageNameOrPath, PackageManager.GET_SIGNATURES)
                        ?.signatures
                        ?.firstOrNull()
                } else null
        } else {
            // API 28以下使用旧版签名获取方式
            @Suppress("DEPRECATION")
            getPackageInfo(packageNameOrPath, PackageManager.GET_SIGNATURES)
                ?.signatures
                ?.firstOrNull()
        }
    }
}