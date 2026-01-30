package com.opticalshop.presentation.screens.profile

import com.opticalshop.data.model.User

data class ProfileState(
    val user: com.opticalshop.data.model.User? = null,
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val isUpdated: Boolean = false,
    val error: String? = null
)
