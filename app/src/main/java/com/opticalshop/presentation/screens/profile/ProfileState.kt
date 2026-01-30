package com.opticalshop.presentation.screens.profile

import com.opticalshop.domain.model.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdated: Boolean = false,
    
    // Editable fields
    val name: String = "",
    val email: String = ""
)
