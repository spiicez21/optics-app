package com.opticalshop.data.repository

import com.opticalshop.data.model.Order
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(order: Order): Result<String>
    fun getOrderHistory(userId: String): Flow<Result<List<Order>>>
    fun getOrderById(orderId: String): Flow<Result<Order>>
}
