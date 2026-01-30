package com.opticalshop.domain.usecase.cart

import com.opticalshop.data.repository.CartRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(userId: String, productId: String, quantity: Int): Result<Unit> {
        return cartRepository.updateCartQuantity(userId, productId, quantity)
    }
}
