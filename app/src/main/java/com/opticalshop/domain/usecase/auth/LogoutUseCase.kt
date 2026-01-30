package com.opticalshop.domain.usecase.auth

import com.google.firebase.auth.FirebaseAuth
import com.opticalshop.data.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
