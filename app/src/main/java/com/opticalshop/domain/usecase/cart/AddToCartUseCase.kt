package com.opticalshop.domain.usecase.cart

import com.opticalshop.data.model.CartItem
import com.opticalshop.data.repository.CartRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(userId: String, cartItem: CartItem): Result<Unit> {
        return cartRepository.addToCart(userId, cartItem)
    }
}
