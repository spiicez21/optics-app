package com.opticalshop.domain.usecase.auth

import com.opticalshop.data.model.User
import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String): Result<User> {
        return authRepository.login(email, pass)
    }
}
