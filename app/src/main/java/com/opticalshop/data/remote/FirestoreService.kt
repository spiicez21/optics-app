package com.opticalshop.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Product
import com.opticalshop.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val subscription = firestore.collection(Constants.PRODUCTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
                    trySend(products)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val subscription = firestore.collection(Constants.CATEGORIES_COLLECTION)
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val categories = snapshot.toObjects(Category::class.java)
                    trySend(categories)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getProductById(productId: String): Flow<Product?> = callbackFlow {
        val subscription = firestore.collection(Constants.PRODUCTS_COLLECTION)
            .document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObject(Product::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }
}
