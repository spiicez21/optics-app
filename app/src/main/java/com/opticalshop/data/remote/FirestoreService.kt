package com.opticalshop.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.opticalshop.data.model.CartItem
import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Order
import com.opticalshop.data.model.Product
import com.opticalshop.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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

    // Cart Operations
    fun getCartItems(userId: String): Flow<List<CartItem>> = callbackFlow {
        val subscription = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.CART_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(CartItem::class.java)
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addToCart(userId: String, cartItem: CartItem) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.CART_COLLECTION)
            .document(cartItem.productId)
            .set(cartItem)
            .await()
    }

    suspend fun updateCartQuantity(userId: String, productId: String, quantity: Int) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.CART_COLLECTION)
            .document(productId)
            .update("quantity", quantity)
            .await()
    }

    suspend fun removeFromCart(userId: String, productId: String) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.CART_COLLECTION)
            .document(productId)
            .delete()
            .await()
    }

    // Order Operations
    suspend fun placeOrder(userId: String, order: Order) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.ORDERS_COLLECTION)
            .document(order.id)
            .set(order)
            .await()
    }

    suspend fun clearCart(userId: String) {
        val cartRef = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.CART_COLLECTION)
        
        val snapshot = cartRef.get().await()
        firestore.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
        }.await()
    }

    fun getOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val subscription = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.ORDERS_COLLECTION)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                    trySend(orders)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getOrderById(userId: String, orderId: String): Flow<Order?> = callbackFlow {
        val subscription = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.ORDERS_COLLECTION)
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObject(Order::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }
}
