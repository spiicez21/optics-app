package com.opticalshop.presentation.screens.auth.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _state = mutableStateOf<Result<Unit>?>(null)
    val state: State<Result<Unit>?> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onNameChange(newValue: String) {
        _name.value = newValue
    }

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun register() {
        viewModelScope.launch {
            _state.value = Result.Loading
            when (val result = registerUseCase(email.value, password.value, name.value)) {
                is Result.Success -> {
                    _state.value = Result.Success(Unit)
                    _eventFlow.emit(UiEvent.NavigateToHome)
                }
                is Result.Error -> {
                    _state.value = Result.Error(result.exception)
                    _eventFlow.emit(UiEvent.ShowError(result.exception.message ?: "An error occurred"))
                }
                Result.Loading -> {}
            }
        }
    }

    sealed class UiEvent {
        object NavigateToHome : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
