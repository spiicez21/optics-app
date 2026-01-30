package com.opticalshop.domain.usecase.user

import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Result<User>> {
        return userRepository.getProfile(userId)
    }
}
