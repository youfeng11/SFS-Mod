package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainViewModel : ViewModel() {

    // 计时器
    private val initialTimer = 3
    var timer by mutableStateOf(initialTimer)
        private set

    // 状态
    var state by mutableStateOf<ScreenState>(ScreenState.Loading)
        private set

    // 错误信息
    var errorMessage by mutableStateOf("")
        private set

    // 计时器递减
    fun decrementTimer() {
        if (timer > 0) timer--
    }

    // 状态管理
    fun setLoadingState() {
        state = ScreenState.Loading
    }

    fun setStoppedState() {
        state = ScreenState.Stopped
    }

    fun setDoneState() {
        state = ScreenState.Done
        timer = initialTimer
    }

    fun setErrorState(message: String) {
        errorMessage = message
        state = ScreenState.Error(message)
    }

    // 定义更清晰的状态
    sealed class ScreenState {
        object Loading : ScreenState()
        object Stopped : ScreenState()
        object Done : ScreenState()
        data class Error(val message: String) : ScreenState()
    }
}