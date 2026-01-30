package com.opticalshop.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val shippingAddress: Address = Address(),
    val paymentMethod: String = "",
    val paymentStatus: String = "pending", // pending, completed, failed
    val orderStatus: String = "pending", // pending, processing, shipped, delivered, cancelled
    val trackingNumber: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
