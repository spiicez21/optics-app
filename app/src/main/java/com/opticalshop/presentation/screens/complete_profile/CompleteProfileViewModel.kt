package com.opticalshop.presentation.screens.complete_profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompleteProfileState(
    val displayName: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val age: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    // Per-field errors
    val phoneError: String? = null,
    val genderError: String? = null,
    val ageError: String? = null
)

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(CompleteProfileState())
    val state: State<CompleteProfileState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser().first()
            firebaseUser?.let { user ->
                userRepository.getProfile(user.id).collectLatest { result ->
                    if (result is Result.Success) {
                        _state.value = _state.value.copy(
                            displayName = result.data.displayName,
                            phoneNumber = result.data.phoneNumber,
                            gender      = result.data.gender,
                            age         = result.data.age,
                            isLoading   = false
                        )
                    }
                }
            }
        }
    }

    fun onPhoneChange(value: String) { _state.value = _state.value.copy(phoneNumber = value, phoneError = null) }
    fun onGenderChange(value: String) { _state.value = _state.value.copy(gender = value, genderError = null) }
    fun onAgeChange(value: String) { _state.value = _state.value.copy(age = value, ageError = null) }

    private fun validate(): Boolean {
        val s = _state.value
        val digits = s.phoneNumber.filter { it.isDigit() }
        val phoneErr = when {
            s.phoneNumber.isBlank()  -> "Phone number is required"
            digits.length != 10      -> "Enter a valid 10-digit phone number"
            else                     -> null
        }
        val genderErr = if (s.gender.isBlank()) "Please select a gender" else null
        val ageErr = when {
            s.age.isBlank() -> "Age is required"
            else -> {
                val n = s.age.toIntOrNull()
                if (n == null || n < 1 || n > 120) "Enter a valid age (1–120)" else null
            }
        }
        _state.value = _state.value.copy(
            phoneError  = phoneErr,
            genderError = genderErr,
            ageError    = ageErr
        )
        return phoneErr == null && genderErr == null && ageErr == null
    }

    fun saveAndContinue() {
        if (!validate()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val firebaseUser = authRepository.getCurrentUser().first()
            firebaseUser?.let { user ->
                val updates = mapOf(
                    "phoneNumber" to _state.value.phoneNumber.filter { it.isDigit() },
                    "gender"      to _state.value.gender,
                    "age"         to _state.value.age
                )
                when (val result = userRepository.updateProfile(user.id, updates)) {
                    is Result.Success -> _eventFlow.emit(UiEvent.NavigateToHome)
                    is Result.Error   -> {
                        _state.value = _state.value.copy(isSaving = false)
                        _eventFlow.emit(UiEvent.ShowError(result.exception.message ?: "Failed to save profile"))
                    }
                    Result.Loading -> {}
                }
            }
        }
    }

    sealed class UiEvent {
        object NavigateToHome : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
