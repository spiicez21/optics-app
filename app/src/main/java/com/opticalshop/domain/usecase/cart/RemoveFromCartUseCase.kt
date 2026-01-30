package com.opticalshop.domain.usecase.cart

import com.opticalshop.data.repository.CartRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class RemoveFromCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(userId: String, productId: String): Result<Unit> {
        return cartRepository.removeFromCart(userId, productId)
    }
}
