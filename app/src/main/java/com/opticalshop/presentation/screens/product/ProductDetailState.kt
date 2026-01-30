package com.opticalshop.presentation.screens.product

import com.opticalshop.data.model.Product

data class ProductDetailState(
    val product: Product? = null,
    val selectedImageIndex: Int = 0,
    val selectedSize: String = "M",
    val quantity: Int = 1,
    val isWishlisted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lensType: String = "",
    val lensMaterial: String = "",
    val lensCoating: String = "",
    val showPrescriptionForm: Boolean = false,
    val sphere: String = "",
    val cylinder: String = "",
    val axis: String = "",
    val add: String = "",
    val pupillaryDistance: String = ""
)
