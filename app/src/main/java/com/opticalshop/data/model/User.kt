package com.opticalshop.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val age: String = "",
    val addresses: List<Address> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null
) {
    val name: String get() = displayName
    
    fun getProfileImageUrl(): String? {
        if (photoUrl.isNotEmpty()) return photoUrl
        return when (gender) {
            "Male" -> "https://i.ibb.co/mCdYRKJT/435bc22dbe77.png"
            "Female" -> "https://i.ibb.co/W435j2dQ/3dd300ddf1b7.png"
            else -> null // Fallback to initials or person icon
        }
    }
}

data class Address(
    val id: String = "",
    val name: String = "",
    val street: String = "",
    val streetAddress: String = street, // ALIAS for UI
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val pincode: String = zipCode, // ALIAS for UI
    val phoneNumber: String = "",
    val landmark: String = "",
    val isDefault: Boolean = false
)
