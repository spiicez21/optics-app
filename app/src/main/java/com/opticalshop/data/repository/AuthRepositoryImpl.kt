package com.opticalshop.data.repository

import com.opticalshop.data.model.User
import com.opticalshop.data.remote.FirebaseAuthService
import com.opticalshop.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: FirebaseAuthService
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = authService.login(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Result.Success(
                    User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: ""
                    )
                )
            } else {
                Result.Error(Exception("Login failed: User is null"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun register(email: String, pass: String, name: String): Result<User> {
        return try {
            val result = authService.register(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Result.Success(
                    User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = name
                    )
                )
            } else {
                Result.Error(Exception("Registration failed: User is null"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun logout() {
        authService.logout()
    }

    override fun getCurrentUser(): Flow<User?> {
        return authService.getCurrentUser().map { firebaseUser ->
            firebaseUser?.let {
                User(
                    id = it.uid,
                    email = it.email ?: "",
                    displayName = it.displayName ?: ""
                )
            }
        }
    }
}
