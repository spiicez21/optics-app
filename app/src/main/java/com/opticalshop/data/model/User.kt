package com.opticalshop.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val addresses: List<Address> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null
)

data class Address(
    val id: String = "",
    val name: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val isDefault: Boolean = false
)
