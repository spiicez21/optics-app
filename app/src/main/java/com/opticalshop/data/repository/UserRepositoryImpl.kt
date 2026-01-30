package com.opticalshop.data.repository

import com.opticalshop.data.model.Address
import com.opticalshop.data.remote.FirestoreService
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : UserRepository {

    override fun getProfile(userId: String): Flow<Result<User>> {
        return firestoreService.getProfile(userId)
            .map { user ->
                if (user != null) {
                    Result.Success(user)
                } else {
                    Result.Error(Exception("User not found"))
                }
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override suspend fun updateProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestoreService.updateProfile(userId, updates)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getAddresses(userId: String): Flow<Result<List<Address>>> {
        return firestoreService.getAddresses(userId)
            .map { addresses ->
                Result.Success(addresses) as Result<List<Address>>
            }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(Exception(e))) }
    }

    override suspend fun addAddress(userId: String, address: Address): Result<Unit> {
        return try {
            firestoreService.addAddress(userId, address)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteAddress(userId: String, addressId: String): Result<Unit> {
        return try {
            firestoreService.deleteAddress(userId, addressId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
