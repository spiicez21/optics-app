package com.opticalshop.domain.usecase.user

import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, updates: Map<String, Any>): Result<Unit> {
        return userRepository.updateProfile(userId, updates)
    }
}
