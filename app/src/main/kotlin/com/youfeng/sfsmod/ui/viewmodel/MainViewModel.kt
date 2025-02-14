package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainViewModel : ViewModel() {
    var timer by mutableStateOf(3)
        private set 

    fun startTimer() {
        timer--
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
}