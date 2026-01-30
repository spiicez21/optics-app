package com.opticalshop.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val address: Address = Address(),
    val paymentMethod: String = "",
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)
