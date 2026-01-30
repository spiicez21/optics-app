package com.opticalshop.presentation.screens.product

import com.opticalshop.data.model.Product

data class ProductDetailState(
    val product: Product? = null,
    val selectedImageIndex: Int = 0,
    val selectedSize: String = "M",
    val quantity: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)
