package com.youfeng.sfsmod.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import com.youfeng.sfsmod.R
import com.youfeng.sfsmod.data.MainRepository
import com.youfeng.sfsmod.utils.vibrate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MainRepository
) : ViewModel() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val state: StateFlow<ScreenState> = _state

    private val _timer = MutableStateFlow(3)
    val timer: StateFlow<Int> = _timer

    val finishEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun setStoppedState() {
        _state.value = ScreenState.Stopped
    }

    fun startCoroutineOnStart() {
        if (_state.value is ScreenState.Loading || _state.value is ScreenState.Done) {
            startCoroutine()
        }
    }

    fun startCoroutine() {
        stopCoroutine()

        _state.value = ScreenState.Loading
        coroutineScope.launch {
            val result = repository.copyResources()
            context.vibrate()
            handleCopyResult(result)
        }
    }

    fun stopCoroutine() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    private suspend fun handleCopyResult(result: Int) {
        when (result) {
            1 -> {
                _state.value = ScreenState.Done
                for (i in 3 downTo 0) {
                    _timer.value = i
                    delay(1000)
                }
                repository.installApk()
                finishEvent.emit(Unit)
            }

            2 -> _state.value = ScreenState.Error(context.getString(R.string.error_sign))
            else -> _state.value = ScreenState.Error(
                context.getString(
                    R.string.error_none,
                    "${Build.BRAND}|${Build.MODEL}|${Build.DEVICE}|${Build.VERSION.SDK_INT}"
                )
            )
        }
    }

    sealed class ScreenState {
        data object Loading : ScreenState()
        data object Stopped : ScreenState()
        data object Done : ScreenState()
        data class Error(val message: String) : ScreenState()
    }
}