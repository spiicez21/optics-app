package com.opticalshop.data.repository

import com.opticalshop.data.model.Cart
import com.opticalshop.data.model.CartItem
import com.opticalshop.data.remote.FirestoreService
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : CartRepository {

    override fun getCart(userId: String): Flow<Result<Cart>> {
        return firestoreService.getCartItems(userId)
            .map { items ->
                Result.Success(Cart(userId, items)) as Result<Cart>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override suspend fun addToCart(userId: String, item: CartItem): Result<Unit> {
        return try {
            firestoreService.addToCart(userId, item)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateQuantity(userId: String, productId: String, quantity: Int): Result<Unit> {
        return try {
            firestoreService.updateCartQuantity(userId, productId, quantity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun removeFromCart(userId: String, productId: String): Result<Unit> {
        return try {
            firestoreService.removeFromCart(userId, productId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
