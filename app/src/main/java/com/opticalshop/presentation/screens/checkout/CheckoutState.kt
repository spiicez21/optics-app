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

    // Address field errors
    val nameError: String? = null,
    val phoneError: String? = null,
    val streetError: String? = null,
    val cityError: String? = null,
    val pincodeError: String? = null,

    // Payment fields
    val paymentMethod: String = "COD", // COD | UPI

    // Dummy payment gateway (UPI)
    val showPaymentGateway: Boolean = false,
    val isPaymentProcessing: Boolean = false,
    val isPaymentSuccess: Boolean = false,
    val upiId: String = "",
    val selectedUpiApp: String = "",
    val paymentError: String? = null,

    val isLoading: Boolean = false,
    val error: String? = null,
    val isOrderPlaced: Boolean = false
)
