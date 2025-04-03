package com.youfeng.sfsmod.utils

import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * 将字节数组转换为SHA-256哈希字符串。
 * 使用Okio库的ByteString进行中间转换以提高性能。
 * @return SHA-256的小写字符串
 */
fun ByteArray.sha256(): String = toByteString().sha256().toHex()

/**
 * 将字节数组转换为十六进制字符串。
 * @return 由两个大写字母组成的十六进制字符串序列，如 "0A1B2C"
 */
fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }

/**
 * 将Okio的ByteString对象转换为十六进制字符串。
 * @return 与[toHexString]相同格式的字符串
 */
private fun ByteString.toHex(): String = toByteArray().toHexString()
