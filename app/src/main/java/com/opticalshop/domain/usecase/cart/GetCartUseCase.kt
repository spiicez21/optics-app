package com.opticalshop.domain.usecase.cart

import com.opticalshop.data.model.CartItem
import com.opticalshop.data.repository.CartRepository
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<CartItem>>> {
        return cartRepository.getCartItems(userId)
    }
}
