package com.opticalshop.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Cart(
    val userId: String = "",
    val items: List<CartItem> = emptyList()
)

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val prescriptionData: PrescriptionData? = null,
    val lensOptions: LensOptions? = null,
    @ServerTimestamp
    val addedAt: Date? = null
)

data class PrescriptionData(
    val sphere: String = "",
    val cylinder: String = "",
    val axis: String = "",
    val add: String = "",
    val pupillaryDistance: String = ""
)

data class LensOptions(
    val type: String = "",
    val coating: String = "",
    val material: String = ""
)
