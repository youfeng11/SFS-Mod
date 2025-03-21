package com.youfeng.sfsmod.utils

import okio.ByteString
import okio.ByteString.Companion.toByteString

fun ByteArray.md5(): String = toByteString().md5().toHex()

fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }

private fun ByteString.toHex(): String = toByteArray().toHexString()
