package com.opticalshop.presentation.screens.address

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.Address
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.user.AddAddressUseCase
import com.opticalshop.domain.usecase.user.DeleteAddressUseCase
import com.opticalshop.domain.usecase.user.GetAddressesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = mutableStateOf(AddressState())
    val state: State<AddressState> = _state

    init {
        loadAddresses()
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                getAddressesUseCase(user.id).collect { result ->
                    when (result) {
                        is Result.Success -> _state.value = _state.value.copy(addresses = result.data, isLoading = false)
                        is Result.Error   -> _state.value = _state.value.copy(isLoading = false, error = result.exception.message)
                        Result.Loading    -> _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun onFullNameChange(value: String) { _state.value = _state.value.copy(fullName = value, nameError = null) }
    fun onPhoneChange(value: String) { _state.value = _state.value.copy(phoneNumber = value, phoneError = null) }
    fun onAddressChange(value: String) { _state.value = _state.value.copy(streetAddress = value, streetError = null) }
    fun onCityChange(value: String) { _state.value = _state.value.copy(city = value, cityError = null) }
    fun onLandmarkChange(value: String) { _state.value = _state.value.copy(landmark = value) }
    fun onPincodeChange(value: String) { _state.value = _state.value.copy(pincode = value, pincodeError = null) }
    fun onDefaultChange(value: Boolean) { _state.value = _state.value.copy(isDefault = value) }

    private fun validate(): Boolean {
        val s = _state.value

        val nameErr = when {
            s.fullName.isBlank()         -> "Full name is required"
            s.fullName.trim().length < 2 -> "Name must be at least 2 characters"
            else                         -> null
        }
        val phoneDigits = s.phoneNumber.filter { it.isDigit() }
        val phoneErr = when {
            s.phoneNumber.isBlank()  -> "Phone number is required"
            phoneDigits.length != 10 -> "Enter a valid 10-digit phone number"
            else                     -> null
        }
        val streetErr  = if (s.streetAddress.isBlank()) "Street address is required" else null
        val cityErr    = if (s.city.isBlank()) "City is required" else null
        val pincodeDigits = s.pincode.filter { it.isDigit() }
        val pincodeErr = when {
            s.pincode.isBlank()       -> "Pincode is required"
            pincodeDigits.length != 6 -> "Enter a valid 6-digit pincode"
            else                      -> null
        }

        _state.value = _state.value.copy(
            nameError    = nameErr,
            phoneError   = phoneErr,
            streetError  = streetErr,
            cityError    = cityErr,
            pincodeError = pincodeErr
        )
        return nameErr == null && phoneErr == null && streetErr == null && cityErr == null && pincodeErr == null
    }

    fun saveAddress() {
        if (!validate()) return
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                _state.value = _state.value.copy(isLoading = true)
                val address = Address(
                    id            = _state.value.addressId.ifBlank { UUID.randomUUID().toString() },
                    name          = _state.value.fullName.trim(),
                    phoneNumber   = _state.value.phoneNumber.filter { it.isDigit() },
                    streetAddress = _state.value.streetAddress.trim(),
                    city          = _state.value.city.trim(),
                    landmark      = _state.value.landmark.trim(),
                    pincode       = _state.value.pincode.trim(),
                    isDefault     = _state.value.isDefault
                )
                when (val result = addAddressUseCase(user.id, address)) {
                    is Result.Success -> _state.value = _state.value.copy(isLoading = false, isAddressAdded = true)
                    is Result.Error   -> _state.value = _state.value.copy(isLoading = false, error = result.exception.message)
                    Result.Loading    -> {}
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) deleteAddressUseCase(user.id, addressId)
        }
    }
}
