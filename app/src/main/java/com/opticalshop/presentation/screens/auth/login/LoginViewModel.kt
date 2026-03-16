package com.opticalshop.presentation.screens.auth.login

import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.LoginUseCase
import com.opticalshop.domain.usecase.auth.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _passwordError = mutableStateOf<String?>(null)
    val passwordError: State<String?> = _passwordError

    private val _state = mutableStateOf<Result<Unit>?>(null)
    val state: State<Result<Unit>?> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChange(newValue: String) {
        _email.value = newValue
        _emailError.value = null
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
        _passwordError.value = null
    }

    private fun validate(): Boolean {
        var valid = true
        if (_email.value.isBlank()) {
            _emailError.value = "Email is required"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_email.value.trim()).matches()) {
            _emailError.value = "Enter a valid email address"
            valid = false
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "Password is required"
            valid = false
        } else if (_password.value.length < 6) {
            _passwordError.value = "Password must be at least 6 characters"
            valid = false
        }
        return valid
    }

    fun login() {
        if (!validate()) return
        viewModelScope.launch {
            _state.value = Result.Loading
            when (val result = loginUseCase(email.value.trim(), password.value)) {
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

    fun onGoogleLogin(idToken: String, displayName: String = "", photoUrl: String = "") {
        viewModelScope.launch {
            _state.value = Result.Loading
            when (val result = loginWithGoogleUseCase(idToken, displayName, photoUrl)) {
                is Result.Success -> {
                    _state.value = Result.Success(Unit)
                    val user = result.data
                    val profileIncomplete = user.phoneNumber.isBlank() || user.gender.isBlank() || user.age.isBlank()
                    if (profileIncomplete) {
                        _eventFlow.emit(UiEvent.NavigateToCompleteProfile)
                    } else {
                        _eventFlow.emit(UiEvent.NavigateToHome)
                    }
                }
                is Result.Error -> {
                    _state.value = Result.Error(result.exception)
                    _eventFlow.emit(UiEvent.ShowError(result.exception.message ?: "Google Sign-In Failed"))
                }
                Result.Loading -> {}
            }
        }
    }

    sealed class UiEvent {
        object NavigateToHome : UiEvent()
        object NavigateToCompleteProfile : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
