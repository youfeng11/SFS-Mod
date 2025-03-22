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

@SuppressLint("NewApi")
@Suppress("DEPRECATION")
fun PackageInfo.signatureMD5(isOld: Boolean = false): String? =
    if (!isOld) signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
        ?.md5() else signatures?.firstOrNull()?.toByteArray()?.md5()

fun Context.installApk(apkPath: Path) {
    val apkFile = File(apkPath.toString()) // 仍然需要转换为 File
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
