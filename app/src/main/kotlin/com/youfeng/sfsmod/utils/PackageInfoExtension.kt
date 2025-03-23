package com.youfeng.sfsmod.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import okio.Path
import java.io.File

/**
 * 获取PackageInfo对象中签名的MD5值。
 * @param isOld 是否使用旧版签名API（signatures字段），默认为false。
 *              true：适用于API 25及以下（Android 7.1及更早版本）
 *              false：使用新版signingInfo字段（API 26+）
 * @return 签名数据的MD5字符串，若签名不存在返回null
 */
@SuppressLint("NewApi")
@Suppress("DEPRECATION")
fun PackageInfo.signatureMD5(isOld: Boolean = false): String? =
    if (!isOld) signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
        ?.md5() else signatures?.firstOrNull()?.toByteArray()?.md5()

/**
 * 通过系统安装界面安装指定路径的APK文件。
 * @param apkPath APK文件的路径（使用Okio Path类型）
 * 实现逻辑：
 * 1. 根据Android版本生成不同的Uri：
 *    - API 24+（Android 7.0+）使用FileProvider适配分区存储
 *    - 旧版本直接使用文件Uri
 * 2. 授予临时读取权限(FLAG_GRANT_READ_URI_PERMISSION)
 * 3. 启动系统安装界面
 */
fun Context.installApk(apkPath: Path) {
    val apkFile = File(apkPath.toString()) // 转换为File对象以兼容系统API
    val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(this, "$packageName.provider", apkFile)
    } else {
        Uri.fromFile(apkFile)
    }

    Intent(Intent.ACTION_VIEW).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        setDataAndType(apkUri, "application/vnd.android.package-archive")
    }.also { startActivity(it) }
}
