package com.opticalshop.presentation.screens.home

import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Product

data class HomeState(
    val userName: String = "Guest",
    val profileImageUrl: String? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String = "all",
    val featuredProducts: List<Product> = emptyList(),
    val popularProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
