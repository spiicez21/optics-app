package com.opticalshop.presentation.screens.checkout

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.Address
import com.opticalshop.data.model.Order
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.cart.GetCartUseCase
import com.opticalshop.domain.usecase.order.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
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

    // ── Address handlers ──
    fun onFullNameChange(value: String) { _state.value = _state.value.copy(fullName = value, nameError = null) }
    fun onPhoneChange(value: String) { _state.value = _state.value.copy(phoneNumber = value, phoneError = null) }
    fun onAddressChange(value: String) { _state.value = _state.value.copy(streetAddress = value, streetError = null) }
    fun onCityChange(value: String) { _state.value = _state.value.copy(city = value, cityError = null) }
    fun onLandmarkChange(value: String) { _state.value = _state.value.copy(landmark = value) }
    fun onPincodeChange(value: String) {
        _state.value = _state.value.copy(pincode = value, pincodeError = null)
        if (value.length == 6) fetchCityFromPincode(value)
    }

    private fun fetchCityFromPincode(pincode: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isFetchingCity = true)
            try {
                val city = withContext(Dispatchers.IO) {
                    val json = URL("https://api.postalpincode.in/pincode/$pincode").readText()
                    val root = JSONArray(json).getJSONObject(0)
                    if (root.getString("Status") == "Success") {
                        val postOffice = root.getJSONArray("PostOffice").getJSONObject(0)
                        postOffice.getString("District")
                    } else null
                }
                if (city != null) {
                    _state.value = _state.value.copy(city = city, cityError = null, isFetchingCity = false)
                } else {
                    _state.value = _state.value.copy(isFetchingCity = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isFetchingCity = false)
            }
        }
    }

    // ── Payment handlers ──
    fun onPaymentMethodChange(value: String) {
        _state.value = _state.value.copy(
            paymentMethod = value,
            paymentError = null,
            shouldLaunchRazorpay = false
        )
    }

    fun onRazorpayFlowLaunched() {
        _state.value = _state.value.copy(
            shouldLaunchRazorpay = false
        )
    }

    fun onRazorpayPaymentSuccess(paymentId: String) {
        _state.value = _state.value.copy(
            isRazorpayPaymentSuccessful = true,
            razorpayPaymentId = paymentId,
            paymentError = null,
            currentStep = CheckoutStep.SUMMARY
        )
    }

    fun onRazorpayPaymentFailed(message: String?) {
        _state.value = _state.value.copy(
            isRazorpayPaymentSuccessful = false,
            razorpayPaymentId = "",
            shouldLaunchRazorpay = false,
            paymentError = message ?: "Payment cancelled"
        )
    }

    fun nextStep() {
        val current = _state.value.currentStep
        when (current) {
            CheckoutStep.ADDRESS -> {
                if (validateAddress()) {
                    _state.value = _state.value.copy(currentStep = CheckoutStep.PAYMENT, error = null)
                }
            }
            CheckoutStep.PAYMENT -> {
                if (_state.value.paymentMethod == "RAZORPAY") {
                    _state.value = _state.value.copy(shouldLaunchRazorpay = true, paymentError = null)
                } else {
                    _state.value = _state.value.copy(currentStep = CheckoutStep.SUMMARY)
                }
            }
            CheckoutStep.SUMMARY -> {
                placeOrder()
            }
        }
    }

    fun previousStep() {
        val prev = when (_state.value.currentStep) {
            CheckoutStep.ADDRESS -> CheckoutStep.ADDRESS
            CheckoutStep.PAYMENT -> CheckoutStep.ADDRESS
            CheckoutStep.SUMMARY -> CheckoutStep.PAYMENT
        }
        _state.value = _state.value.copy(currentStep = prev)
    }

    private fun validateAddress(): Boolean {
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
        val streetErr = if (s.streetAddress.isBlank()) "Street address is required" else null
        val cityErr   = if (s.city.isBlank()) "City is required" else null
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
            pincodeError = pincodeErr,
            error        = null
        )
        return nameErr == null && phoneErr == null && streetErr == null && cityErr == null && pincodeErr == null
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
                    paymentId = _state.value.razorpayPaymentId,
                    paymentStatus = if (_state.value.paymentMethod == "RAZORPAY") "PAID" else "PENDING",
                    status = "PENDING",
                    timestamp = System.currentTimeMillis()
                )
                when (val result = placeOrderUseCase(order)) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(isLoading = false, isOrderPlaced = true)
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.exception.message)
                    }
                    Result.Loading -> {}
                }
            }
        }
    }
}
