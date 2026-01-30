package com.opticalshop.domain.usecase.user

import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class DeleteAddressUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, addressId: String): Result<Unit> {
        return userRepository.deleteAddress(userId, addressId)
    }
}
