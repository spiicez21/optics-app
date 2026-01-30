package com.opticalshop.data.repository

import com.opticalshop.data.model.Address
import com.opticalshop.domain.model.Result
import com.opticalshop.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getProfile(userId: String): Flow<Result<User>>
    suspend fun updateProfile(userId: String, updates: Map<String, Any>): Result<Unit>
    fun getAddresses(userId: String): Flow<Result<List<Address>>>
    suspend fun addAddress(userId: String, address: Address): Result<Unit>
    suspend fun deleteAddress(userId: String, addressId: String): Result<Unit>
    
    // Wishlist
    fun getWishlist(userId: String): Flow<com.opticalshop.domain.model.Result<List<com.opticalshop.data.model.Product>>>
    suspend fun addToWishlist(userId: String, product: com.opticalshop.data.model.Product): com.opticalshop.domain.model.Result<Unit>
    suspend fun removeFromWishlist(userId: String, productId: String): com.opticalshop.domain.model.Result<Unit>
}
