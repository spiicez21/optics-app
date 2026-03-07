package com.opticalshop.domain.usecase.auth

import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.domain.model.Result
import com.opticalshop.data.model.User
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        idToken: String,
        displayName: String = "",
        photoUrl: String = ""
    ): Result<User> {
        return repository.loginWithGoogle(idToken, displayName, photoUrl)
    }
}
