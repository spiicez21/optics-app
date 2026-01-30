package com.opticalshop.data.repository

import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Product
import com.opticalshop.data.remote.FirestoreService
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : ProductRepository {

    override fun getProducts(): Flow<Result<List<Product>>> {
        return firestoreService.getProducts()
            .map { products ->
                Result.Success(products) as Result<List<Product>>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun getProductById(productId: String): Flow<Result<Product>> {
        return firestoreService.getProductById(productId)
            .map { product ->
                if (product != null) {
                    Result.Success(product)
                } else {
                    Result.Error(Exception("Product not found"))
                }
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun getCategories(): Flow<Result<List<Category>>> {
        return firestoreService.getCategories()
            .map { categories ->
                Result.Success(categories) as Result<List<Category>>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun searchProducts(query: String): Flow<Result<List<Product>>> {
        // Simple search for now, filtered on client side or via firestore query
        return firestoreService.getProducts()
            .map { products ->
                val filtered = products.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.brand.contains(query, ignoreCase = true) 
                }
                Result.Success(filtered)
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }
}
