package com.youfeng.sfsmod.utils

import android.content.pm.PackageInfo
import java.io.File
import okio.ByteString
import okio.ByteString.Companion.toByteString

fun ByteArray.sha1(): String = toByteString().sha1().toHex()

fun ByteArray.sha256(): String = toByteString().sha256().toHex()

fun ByteArray.md5(): String = toByteString().md5().toHex()

fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }

fun File.md5(): String = readBytes().md5()

private fun ByteString.toHex(): String = toByteArray().toHexString()

@Suppress("DEPRECATION")
fun PackageInfo.signatureMD5(isOld: Boolean = false): String? = if (!isOld) signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()?.md5() else signatures?.firstOrNull()?.toByteArray()?.md5()

