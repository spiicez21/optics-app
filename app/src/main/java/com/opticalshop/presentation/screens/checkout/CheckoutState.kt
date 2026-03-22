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
    val paymentMethod: String = "COD", // COD | RAZORPAY
    val shouldLaunchRazorpay: Boolean = false,
    val isRazorpayPaymentSuccessful: Boolean = false,
    val razorpayPaymentId: String = "",
    val paymentError: String? = null,

    val isFetchingCity: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOrderPlaced: Boolean = false
)
