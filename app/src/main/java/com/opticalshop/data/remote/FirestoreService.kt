package com.opticalshop.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.opticalshop.data.model.Address
import com.opticalshop.data.model.CartItem
import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Order
import com.opticalshop.data.model.Product
import com.opticalshop.data.model.User
import com.opticalshop.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun addProduct(product: Product) {
        firestore.collection(Constants.PRODUCTS_COLLECTION)
            .document(product.id.ifBlank { UUID.randomUUID().toString() })
            .set(product)
            .await()
    }

    suspend fun addCategory(category: Category) {
        firestore.collection(Constants.CATEGORIES_COLLECTION)
            .document(category.id.ifBlank { UUID.randomUUID().toString() })
            .set(category)
            .await()
    }

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

    suspend fun saveProfile(user: User) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun syncUserOnLogin(user: User) {
        val userRef = firestore.collection(Constants.USERS_COLLECTION).document(user.id)
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            userRef.set(user).await()
        } else {
            // Optional: Update basic info if needed, but let's preserve existing data for now
            // userRef.update("displayName", user.displayName, "photoUrl", user.photoUrl).await()
        }
    }

    // Profile & Address Operations
    fun getProfile(userId: String): Flow<User?> = callbackFlow {
        val subscription = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObject(User::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateProfile(userId: String, updates: Map<String, Any>) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .update(updates)
            .await()
    }

    fun getAddresses(userId: String): Flow<List<Address>> = callbackFlow {
        val subscription = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.ADDRESSES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val addresses = snapshot.toObjects(Address::class.java)
                    trySend(addresses)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addAddress(userId: String, address: Address) {
        val addressRef = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.ADDRESSES_COLLECTION)
            .document(address.id.ifBlank { UUID.randomUUID().toString() })
        
        val finalAddress = if (address.id.isBlank()) address.copy(id = addressRef.id) else address
        
        addressRef.set(finalAddress).await()
    }

    suspend fun deleteAddress(userId: String, addressId: String) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.ADDRESSES_COLLECTION)
            .document(addressId)
            .delete()
            .await()
    }

    // Wishlist Operations
    fun getWishlistItems(userId: String): Flow<List<Product>> = callbackFlow {
        val subscription = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.WISHLIST_COLLECTION)
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

    suspend fun addToWishlist(userId: String, product: Product) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.WISHLIST_COLLECTION)
            .document(product.id)
            .set(product)
            .await()
    }

    suspend fun removeFromWishlist(userId: String, productId: String) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.WISHLIST_COLLECTION)
            .document(productId)
            .delete()
            .await()
    }
}
