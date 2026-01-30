package com.opticalshop.presentation.screens.checkout

import com.opticalshop.data.model.CartItem

enum class CheckoutStep {
    ADDRESS,
    PAYMENT,
    SUMMARY
}

data class CheckoutState(
    val currentStep: CheckoutStep = CheckoutStep.ADDRESS,
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    
    // Address fields
    val fullName: String = "",
    val phoneNumber: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val landmark: String = "",
    val pincode: String = "",
    
    // Payment fields
    val paymentMethod: String = "COD", // Default to Cash on Delivery
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOrderPlaced: Boolean = false
)
