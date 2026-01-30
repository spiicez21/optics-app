package com.opticalshop.data.repository

import com.opticalshop.data.model.Order
import com.opticalshop.data.remote.FirestoreService
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : OrderRepository {

    override suspend fun placeOrder(userId: String, order: Order): Result<Unit> {
        return try {
            firestoreService.placeOrder(userId, order)
            firestoreService.clearCart(userId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getOrders(userId: String): Flow<Result<List<Order>>> {
        // Implementation for getting orders could be added later if needed
        return firestoreService.getOrders(userId)
            .map { orders ->
                Result.Success(orders) as Result<List<Order>>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun getOrderById(userId: String, orderId: String): Flow<Result<Order>> {
        // Implementation for getting order by id could be added later
        return firestoreService.getOrderById(userId, orderId)
            .map { order ->
                if (order != null) {
                    Result.Success(order)
                } else {
                    Result.Error(Exception("Order not found"))
                }
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }
}
