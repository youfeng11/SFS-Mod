package com.youfeng.sfsmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.youfeng.sfsmod.data.VerifySignatureStates
import com.youfeng.sfsmod.data.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repository: MainRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val state: StateFlow<ScreenState> = _state

    private val _timer = MutableStateFlow(3)
    val timer: StateFlow<Int> = _timer

    fun setStoppedState() {
        _state.update { ScreenState.Stopped }
    }

    fun startCoroutineOnStart() {
        if (state.value is ScreenState.Loading || state.value is ScreenState.Done) {
            startCoroutine()
        }
    }

    fun startCoroutine() {
        stopCoroutine()

        _state.update { ScreenState.Loading }
        coroutineScope.launch {
            val result = repository.copyResources()
            _uiEvent.emit(UiEvent.Vibrate)
            handleCopyResult(result)
        }
    }

    fun stopCoroutine() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    private suspend fun handleCopyResult(result: VerifySignatureStates) {
        when (result) {
            is VerifySignatureStates.SignatureValid -> {
                _state.update { ScreenState.Done }
                for (i in 3 downTo 0) {
                    _timer.update { i }
                    delay(1000)
                }
                _uiEvent.emit(UiEvent.NavigateToApkInstall)
                _uiEvent.emit(UiEvent.Finish)
            }

            is VerifySignatureStates.SignatureMismatch -> _state.update { ScreenState.Error(ErrorType.SignatureMismatch) }
            is VerifySignatureStates.SignatureUnavailable -> _state.update { ScreenState.Error(ErrorType.SignatureUnavailable) }
        }
    }

    sealed class ScreenState {
        data object Loading : ScreenState()
        data object Stopped : ScreenState()
        data object Done : ScreenState()
        data class Error(val errorType: ErrorType) : ScreenState() // 使用 ErrorType 代替 String
    }

    sealed class ErrorType {
        data object SignatureMismatch : ErrorType()
        data object SignatureUnavailable : ErrorType()
    }

    sealed class UiEvent {
        data object NavigateToApkInstall : UiEvent()
        data object Vibrate : UiEvent()
        data object Finish : UiEvent()
    }
}