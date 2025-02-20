package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainViewModel : ViewModel() {
    var timer by mutableStateOf(3)
        private set 
    var errorInfo by mutableStateOf("")
        private set 

    fun startTimer() {
        timer--
    }


    fun SignError() {
        errorInfo = "签名不一致，安装无法进行！\n安装器可能被篡改，请从官方渠道（应用关于内的QQ群或123网盘）重新下载！"
    }
    fun VersionError() {
        errorInfo = "版本号不一致，安装无法进行！\n安装器可能被篡改，请从官方渠道（应用关于内的QQ群或123网盘）重新下载！"
    }

    var state by mutableStateOf(0)
        private set

    fun loadingState() {
        state = 0
    }
    fun stoppedState() {
        state = 1
    }
    fun doneState() {
        state = 2
        timer = 3
    }
    fun errorState() {
        state = 3
    }
}