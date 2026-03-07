package com.opticalshop.presentation.screens.profile

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.remote.FirebaseStorageService
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
    private val logoutUseCase: LogoutUseCase,
    private val storageService: FirebaseStorageService
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
                                profileImageUrl = result.data.getProfileImageUrl(),
                                isLoading = false,
                                error = null
                            )
                        }
                        is Result.Error -> {
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

    fun uploadProfileImage(uri: Uri) {
        val userId = _state.value.user?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploadingImage = true, error = null)
            try {
                val downloadUrl = storageService.uploadProfileImage(userId, uri)
                updateProfileUseCase(userId, mapOf("photoUrl" to downloadUrl))
                _state.value = _state.value.copy(
                    profileImageUrl = downloadUrl,
                    isUploadingImage = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUploadingImage = false,
                    error = "Failed to upload image: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
