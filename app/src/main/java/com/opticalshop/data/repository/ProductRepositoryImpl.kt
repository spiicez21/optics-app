package com.opticalshop.data.repository

import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Product
import com.opticalshop.data.remote.FirestoreService
import com.opticalshop.di.ApplicationScope
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    @ApplicationScope private val appScope: CoroutineScope
) : ProductRepository {

    /**
     * Single shared Firestore listener for products. All subscribers reuse this
     * stream — no duplicate listeners are opened even across multiple screens.
     * The listener stays alive for 5 s after the last subscriber drops off so
     * fast screen transitions don't cause an unnecessary reconnect.
     */
    private val productsShared: SharedFlow<List<Product>> = firestoreService.getProducts()
        .catch { /* Absorb Firestore errors (e.g. PERMISSION_DENIED) so appScope doesn't crash */ }
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    /** Same sharing strategy for categories. */
    private val categoriesShared: SharedFlow<List<Category>> = firestoreService.getCategories()
        .catch { /* Absorb Firestore errors (e.g. PERMISSION_DENIED) so appScope doesn't crash */ }
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    override fun getProducts(): Flow<Result<List<Product>>> {
        return productsShared
            .map { products -> Result.Success(products) as Result<List<Product>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun getProductById(productId: String): Flow<Result<Product>> {
        return firestoreService.getProductById(productId)
            .map { product ->
                if (product != null) Result.Success(product) as Result<Product>
                else Result.Error(Exception("Product not found")) as Result<Product>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun getCategories(): Flow<Result<List<Category>>> {
        return categoriesShared
            .map { categories -> Result.Success(categories) as Result<List<Category>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    /**
     * Filters the already-cached products stream rather than opening a new
     * Firestore listener, eliminating redundant reads on every search keystroke.
     */
    override fun searchProducts(query: String): Flow<Result<List<Product>>> {
        return productsShared
            .map { products ->
                val filtered = if (query.isBlank()) products else products.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        it.brand.contains(query, ignoreCase = true)
                }
                Result.Success(filtered) as Result<List<Product>>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override fun getReviews(productId: String): Flow<Result<List<com.opticalshop.domain.model.Review>>> {
        return firestoreService.getReviews(productId)
            .map { reviews -> Result.Success(reviews) as Result<List<com.opticalshop.domain.model.Review>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override suspend fun addReview(review: com.opticalshop.domain.model.Review): Result<Unit> {
        return try {
            firestoreService.addReview(review)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
