package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainViewModel : ViewModel() {/*
    var isStop by mutableStateOf(false)
        private set
    var isDone by mutableStateOf(false)
        private set

    fun onIsStop() {
        isStop = true
    }
    fun offIsStop() {
        isStop = false
    }
    fun onIsDone() {
        isDone = true
    }
    fun offIsDone() {
        isDone = false
    }*/


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
    }
}