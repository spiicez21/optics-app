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

    override suspend fun placeOrder(order: Order): Result<String> {
        return try {
            firestoreService.placeOrder(order.userId, order)
            firestoreService.clearCart(order.userId)
            Result.Success(order.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getOrderHistory(userId: String): Flow<Result<List<Order>>> {
        return firestoreService.getOrders(userId)
            .map { orders ->
                Result.Success(orders) as Result<List<Order>>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun getOrderById(userId: String, orderId: String): Flow<Result<Order>> {
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
