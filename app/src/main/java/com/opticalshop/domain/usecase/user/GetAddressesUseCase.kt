package com.opticalshop.domain.usecase.user

import com.opticalshop.data.model.Address
import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAddressesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<Address>>> {
        return userRepository.getAddresses(userId)
    }
}
