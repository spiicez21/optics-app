package com.opticalshop.presentation.screens.checkout

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.Address
import com.opticalshop.data.model.Cart
import com.opticalshop.data.model.CartItem
import com.opticalshop.data.model.Order
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.cart.GetCartUseCase
import com.opticalshop.domain.usecase.order.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userRepository: com.opticalshop.data.repository.UserRepository
) : ViewModel() {

    private val _state = mutableStateOf(CheckoutState())
    val state: State<CheckoutState> = _state

    init {
        loadCart()
        autoFillFromProfile()
    }

    private fun autoFillFromProfile() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                userRepository.getProfile(user.id).collect { result ->
                    if (result is Result.Success) {
                        val profile = result.data
                        val mainAddress = profile.addresses.find { it.isDefault } ?: profile.addresses.firstOrNull()
                        
                        _state.value = _state.value.copy(
                            fullName = profile.displayName.ifBlank { _state.value.fullName },
                            phoneNumber = profile.phoneNumber.ifBlank { _state.value.phoneNumber },
                            streetAddress = mainAddress?.streetAddress ?: _state.value.streetAddress,
                            city = mainAddress?.city ?: _state.value.city,
                            pincode = mainAddress?.pincode ?: _state.value.pincode,
                            landmark = mainAddress?.landmark ?: _state.value.landmark
                        )
                    }
                }
            }
        }
    }

    private fun loadCart() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                getCartUseCase(user.id).collect { result ->
                    if (result is Result.Success) {
                        val cart = result.data
                        val total = cart.items.sumOf { it.price * it.quantity }
                        _state.value = _state.value.copy(
                            cartItems = cart.items,
                            totalAmount = total
                        )
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
    fun onPaymentMethodChange(value: String) { _state.value = _state.value.copy(paymentMethod = value) }

    fun nextStep() {
        val current = _state.value.currentStep
        when (current) {
            CheckoutStep.ADDRESS -> {
                if (validateAddress()) {
                    _state.value = _state.value.copy(currentStep = CheckoutStep.PAYMENT)
                }
            }
            CheckoutStep.PAYMENT -> {
                _state.value = _state.value.copy(currentStep = CheckoutStep.SUMMARY)
            }
            CheckoutStep.SUMMARY -> {
                placeOrder()
            }
        }
    }

    fun previousStep() {
        val current = _state.value.currentStep
        val prev = when (current) {
            CheckoutStep.ADDRESS -> CheckoutStep.ADDRESS
            CheckoutStep.PAYMENT -> CheckoutStep.ADDRESS
            CheckoutStep.SUMMARY -> CheckoutStep.PAYMENT
        }
        _state.value = _state.value.copy(currentStep = prev)
    }

    private fun validateAddress(): Boolean {
        with(_state.value) {
            if (fullName.isBlank() || phoneNumber.isBlank() || streetAddress.isBlank() || city.isBlank() || pincode.isBlank()) {
                _state.value = _state.value.copy(error = "Please fill all required fields")
                return false
            }
        }
        _state.value = _state.value.copy(error = null)
        return true
    }

    private fun placeOrder() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                val address = with(_state.value) {
                    Address(
                        name = fullName,
                        phoneNumber = phoneNumber,
                        streetAddress = streetAddress,
                        city = city,
                        landmark = landmark,
                        pincode = pincode,
                        isDefault = true
                    )
                }
                
                val order = Order(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    items = _state.value.cartItems,
                    totalAmount = _state.value.totalAmount,
                    address = address,
                    paymentMethod = _state.value.paymentMethod,
                    status = "PENDING",
                    timestamp = System.currentTimeMillis()
                )
                
                when (val result = placeOrderUseCase(order)) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isOrderPlaced = true
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
}
