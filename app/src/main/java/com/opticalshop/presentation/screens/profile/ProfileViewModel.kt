package com.opticalshop.presentation.screens.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.auth.LogoutUseCase
import com.opticalshop.domain.usecase.user.GetProfileUseCase
import com.opticalshop.domain.usecase.user.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val firebaseUser = getCurrentUserUseCase().first()
            if (firebaseUser != null) {
                // Pre-fill from Firebase Auth while loading from Firestore
                _state.value = _state.value.copy(
                    name = firebaseUser.displayName,
                    email = firebaseUser.email
                )

                getProfileUseCase(firebaseUser.id).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _state.value = _state.value.copy(
                                user = result.data,
                                name = if (result.data.displayName.isNotBlank()) result.data.displayName else firebaseUser.displayName,
                                email = if (result.data.email.isNotBlank()) result.data.email else firebaseUser.email,
                                isLoading = false,
                                error = null
                            )
                        }
                        is Result.Error -> {
                            // If Firestore profile is missing, we still have the Firebase info
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = if (result.exception.message == "User not found") null else result.exception.message
                            )
                        }
                        Result.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value) }

    fun updateProfile() {
        viewModelScope.launch {
            val user = _state.value.user
            if (user != null) {
                _state.value = _state.value.copy(isLoading = true)
                val updates = mapOf("name" to _state.value.name)
                when (val result = updateProfileUseCase(user.id, updates)) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isUpdated = true
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                    Result.Loading -> {}
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
