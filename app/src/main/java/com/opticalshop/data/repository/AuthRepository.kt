package com.opticalshop.data.repository

import com.opticalshop.data.model.User
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun register(
        email: String,
        pass: String,
        name: String,
        phoneNumber: String = "",
        gender: String = "",
        age: String = ""
    ): Result<User>
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
}
