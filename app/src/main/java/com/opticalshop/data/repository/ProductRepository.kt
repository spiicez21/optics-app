package com.opticalshop.data.repository

import com.opticalshop.data.model.Product
import com.opticalshop.data.model.Category
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<Result<List<Product>>>
    fun getProductById(productId: String): Flow<Result<Product>>
    fun getCategories(): Flow<Result<List<Category>>>
    fun searchProducts(query: String): Flow<Result<List<Product>>>
}
