package com.opticalshop.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double? = null,
    val category: String = "",
    val frameShape: String = "",
    val frameMaterial: String = "",
    val frameColor: String = "",
    val lensType: String = "",
    val gender: String = "",
    val images: List<String> = emptyList(),
    val stock: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val featured: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)
