package com.opticalshop.presentation.screens.auth.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.RegisterUseCase
import com.opticalshop.domain.usecase.auth.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    fun onGoogleLogin(idToken: String) {
        viewModelScope.launch {
            _state.value = Result.Loading
            when (val result = loginWithGoogleUseCase(idToken)) {
                is Result.Success -> {
                    _state.value = Result.Success(Unit)
                    _eventFlow.emit(UiEvent.NavigateToHome)
                }
                is Result.Error -> {
                    _state.value = Result.Error(result.exception)
                    _eventFlow.emit(UiEvent.ShowError(result.exception.message ?: "Google Sign-In Failed"))
                }
                Result.Loading -> {}
            }
        }
    }

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber

    private val _gender = mutableStateOf("")
    val gender: State<String> = _gender

    private val _age = mutableStateOf("")
    val age: State<String> = _age

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

    fun onPhoneChange(newValue: String) {
        _phoneNumber.value = newValue
    }

    fun onGenderChange(newValue: String) {
        _gender.value = newValue
    }

    fun onAgeChange(newValue: String) {
        _age.value = newValue
    }

    fun register() {
        if (name.value.isBlank() || email.value.isBlank() || password.value.isBlank() || phoneNumber.value.isBlank() || gender.value.isBlank() || age.value.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowError("Please fill all fields"))
            }
            return
        }

        viewModelScope.launch {
            _state.value = Result.Loading
            when (val result = registerUseCase(
                email = email.value,
                pass = password.value,
                name = name.value,
                phoneNumber = phoneNumber.value,
                gender = gender.value,
                age = age.value
            )) {
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
