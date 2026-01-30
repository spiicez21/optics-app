package com.opticalshop.presentation.screens.cart

import com.opticalshop.data.model.CartItem

data class CartState(
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)
