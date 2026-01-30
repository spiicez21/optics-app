package com.opticalshop.data.repository

import com.opticalshop.data.model.Cart
import com.opticalshop.data.model.CartItem
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(userId: String): Flow<Result<Cart>>
    suspend fun addToCart(userId: String, item: CartItem): Result<Unit>
    suspend fun removeFromCart(userId: String, productId: String): Result<Unit>
    suspend fun updateQuantity(userId: String, productId: String, quantity: Int): Result<Unit>
}
