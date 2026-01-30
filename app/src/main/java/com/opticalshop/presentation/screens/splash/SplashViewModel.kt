package com.opticalshop.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                _eventFlow.emit(UiEvent.NavigateToHome)
            } else {
                _eventFlow.emit(UiEvent.NavigateToLogin)
            }
        }
    }

    sealed class UiEvent {
        object NavigateToHome : UiEvent()
        object NavigateToLogin : UiEvent()
    }
}
