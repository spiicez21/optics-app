package com.opticalshop.presentation.screens.personal_info

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonalInfoState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

@HiltViewModel
class PersonalInfoViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(PersonalInfoState())
    val state: State<PersonalInfoState> = _state

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val firebaseUser = authRepository.getCurrentUser().first()
            firebaseUser?.let { user ->
                userRepository.getProfile(user.id).collectLatest { result ->
                    if (result is Result.Success) {
                        _state.value = _state.value.copy(
                            name = result.data.displayName,
                            email = result.data.email,
                            phoneNumber = result.data.phoneNumber,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun onPhoneChange(phone: String) {
        _state.value = _state.value.copy(phoneNumber = phone)
    }

    fun updateProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, error = null)
            val firebaseUser = authRepository.getCurrentUser().first()
            firebaseUser?.let { user ->
                val updates = mapOf(
                    "displayName" to _state.value.name,
                    "phoneNumber" to _state.value.phoneNumber
                )
                val result = userRepository.updateProfile(user.id, updates)
                if (result is Result.Success) {
                    _state.value = _state.value.copy(isUpdating = false, updateSuccess = true)
                } else if (result is Result.Error) {
                    _state.value = _state.value.copy(isUpdating = false, error = result.exception.message)
                }
            }
        }
    }
    
    fun resetSuccess() {
        _state.value = _state.value.copy(updateSuccess = false)
    }
}
