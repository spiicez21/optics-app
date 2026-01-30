package com.opticalshop.presentation.screens.home

import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Product

data class HomeState(
    val categories: List<Category> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
