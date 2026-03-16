package com.opticalshop.presentation.screens.auth.register

import android.util.Patterns
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

    // ── Per-field errors ──
    private val _nameError = mutableStateOf<String?>(null)
    val nameError: State<String?> = _nameError

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _passwordError = mutableStateOf<String?>(null)
    val passwordError: State<String?> = _passwordError

    private val _phoneError = mutableStateOf<String?>(null)
    val phoneError: State<String?> = _phoneError

    private val _genderError = mutableStateOf<String?>(null)
    val genderError: State<String?> = _genderError

    private val _ageError = mutableStateOf<String?>(null)
    val ageError: State<String?> = _ageError

    private val _state = mutableStateOf<Result<Unit>?>(null)
    val state: State<Result<Unit>?> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onNameChange(newValue: String) { _name.value = newValue; _nameError.value = null }
    fun onEmailChange(newValue: String) { _email.value = newValue; _emailError.value = null }
    fun onPasswordChange(newValue: String) { _password.value = newValue; _passwordError.value = null }
    fun onPhoneChange(newValue: String) { _phoneNumber.value = newValue; _phoneError.value = null }
    fun onGenderChange(newValue: String) { _gender.value = newValue; _genderError.value = null }
    fun onAgeChange(newValue: String) { _age.value = newValue; _ageError.value = null }

    private fun validate(): Boolean {
        var valid = true

        if (_name.value.isBlank()) {
            _nameError.value = "Full name is required"
            valid = false
        } else if (_name.value.trim().length < 2) {
            _nameError.value = "Name must be at least 2 characters"
            valid = false
        }

        if (_email.value.isBlank()) {
            _emailError.value = "Email is required"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_email.value.trim()).matches()) {
            _emailError.value = "Enter a valid email address"
            valid = false
        }

        val digits = _phoneNumber.value.filter { it.isDigit() }
        if (_phoneNumber.value.isBlank()) {
            _phoneError.value = "Phone number is required"
            valid = false
        } else if (digits.length != 10) {
            _phoneError.value = "Enter a valid 10-digit phone number"
            valid = false
        }

        if (_age.value.isBlank()) {
            _ageError.value = "Age is required"
            valid = false
        } else {
            val ageInt = _age.value.toIntOrNull()
            if (ageInt == null || ageInt < 1 || ageInt > 120) {
                _ageError.value = "Enter a valid age (1–120)"
                valid = false
            }
        }

        if (_gender.value.isBlank()) {
            _genderError.value = "Please select a gender"
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

    fun register() {
        if (!validate()) return

        viewModelScope.launch {
            _state.value = Result.Loading
            when (val result = registerUseCase(
                email       = _email.value.trim(),
                pass        = _password.value,
                name        = _name.value.trim(),
                phoneNumber = _phoneNumber.value.filter { it.isDigit() },
                gender      = _gender.value,
                age         = _age.value
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
