package com.opticalshop.domain.usecase.user

import com.opticalshop.data.model.Address
import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class AddAddressUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, address: Address): Result<Unit> {
        return userRepository.addAddress(userId, address)
    }
}
