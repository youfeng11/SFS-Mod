package com.youfeng.sfsmod.utils

import android.os.Build

object DeviceInfo {
    val DeviceInfoString: String
        get() = "${Build.BRAND}|${Build.MODEL}|${Build.DEVICE}|${Build.VERSION.SDK_INT}"
}