package com.opticalshop.presentation.screens.address

import com.opticalshop.data.model.Address

data class AddressState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddressAdded: Boolean = false,

    // Fields for add/edit
    val addressId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val landmark: String = "",
    val pincode: String = "",
    val isDefault: Boolean = false,

    // Per-field validation errors
    val nameError: String? = null,
    val phoneError: String? = null,
    val streetError: String? = null,
    val cityError: String? = null,
    val pincodeError: String? = null
)
