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
                        is Result.Success -> {
                            _state.value = _state.value.copy(
                                addresses = result.data,
                                isLoading = false
                            )
                        }
                        is Result.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                        Result.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    fun onFullNameChange(value: String) { _state.value = _state.value.copy(fullName = value) }
    fun onPhoneChange(value: String) { _state.value = _state.value.copy(phoneNumber = value) }
    fun onAddressChange(value: String) { _state.value = _state.value.copy(streetAddress = value) }
    fun onCityChange(value: String) { _state.value = _state.value.copy(city = value) }
    fun onLandmarkChange(value: String) { _state.value = _state.value.copy(landmark = value) }
    fun onPincodeChange(value: String) { _state.value = _state.value.copy(pincode = value) }
    fun onDefaultChange(value: Boolean) { _state.value = _state.value.copy(isDefault = value) }

    fun saveAddress() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                _state.value = _state.value.copy(isLoading = true)
                val address = Address(
                    id = _state.value.addressId.ifBlank { UUID.randomUUID().toString() },
                    name = _state.value.fullName,
                    phoneNumber = _state.value.phoneNumber,
                    streetAddress = _state.value.streetAddress,
                    city = _state.value.city,
                    landmark = _state.value.landmark,
                    pincode = _state.value.pincode,
                    isDefault = _state.value.isDefault
                )
                
                when (val result = addAddressUseCase(user.id, address)) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isAddressAdded = true
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                    Result.Loading -> {}
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                deleteAddressUseCase(user.id, addressId)
            }
        }
    }
}
