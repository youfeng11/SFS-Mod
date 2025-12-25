package com.youfeng.sfsmod.util

import android.os.Build

object DeviceInfo {
    val DeviceInfoString: String
        get() = "${Build.BRAND}|${Build.MODEL}|${Build.DEVICE}|${Build.VERSION.SDK_INT}"
}