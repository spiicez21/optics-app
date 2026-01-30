package com.opticalshop.domain.usecase.order

import com.opticalshop.data.model.Order
import com.opticalshop.data.repository.OrderRepository
import com.opticalshop.domain.model.Result
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(userId: String, order: Order): Result<Unit> {
        return orderRepository.placeOrder(userId, order)
    }
}
