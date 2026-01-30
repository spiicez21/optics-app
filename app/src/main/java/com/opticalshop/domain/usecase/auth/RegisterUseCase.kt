package com.opticalshop.domain.usecase.auth

import com.opticalshop.data.model.User
import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String, name: String): Result<User> {
        return authRepository.register(email, pass, name)
    }
}
