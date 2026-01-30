package com.opticalshop.presentation.screens.product

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.CartItem
import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.cart.AddToCartUseCase
import com.opticalshop.domain.usecase.product.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(ProductDetailState())
    val state: State<ProductDetailState> = _state

    init {
        savedStateHandle.get<String>("productId")?.let { productId ->
            getProduct(productId)
            checkWishlistStatus(productId)
        }
    }

    private fun checkWishlistStatus(productId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                userRepository.getWishlist(user.id).collect { result ->
                    if (result is Result.Success) {
                        val isWishlisted = result.data.any { it.id == productId }
                        _state.value = _state.value.copy(isWishlisted = isWishlisted)
                    }
                }
            }
        }
    }

    fun toggleWishlist() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                if (_state.value.isWishlisted) {
                    userRepository.removeFromWishlist(user.id, product.id)
                } else {
                    userRepository.addToWishlist(user.id, product)
                }
            }
        }
    }

    private fun getProduct(productId: String) {
        viewModelScope.launch {
            getProductByIdUseCase(productId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            product = result.data,
                            isLoading = false
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            error = result.exception.message,
                            isLoading = false
                        )
                    }
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun onSizeSelect(size: String) {
        _state.value = _state.value.copy(selectedSize = size)
    }

    fun onImageSelect(index: Int) {
        _state.value = _state.value.copy(selectedImageIndex = index)
    }

    fun updateQuantity(delta: Int) {
        val newQty = (_state.value.quantity + delta).coerceAtLeast(1)
        _state.value = _state.value.copy(quantity = newQty)
    }

    fun onLensTypeSelect(type: String) { _state.value = _state.value.copy(lensType = type) }
    fun onLensMaterialSelect(material: String) { _state.value = _state.value.copy(lensMaterial = material) }
    fun onLensCoatingSelect(coating: String) { _state.value = _state.value.copy(lensCoating = coating) }
    fun togglePrescriptionForm() { _state.value = _state.value.copy(showPrescriptionForm = !_state.value.showPrescriptionForm) }
    fun onSphereChange(value: String) { _state.value = _state.value.copy(sphere = value) }
    fun onCylinderChange(value: String) { _state.value = _state.value.copy(cylinder = value) }
    fun onAxisChange(value: String) { _state.value = _state.value.copy(axis = value) }
    fun onAddChange(value: String) { _state.value = _state.value.copy(add = value) }
    fun onPdChange(value: String) { _state.value = _state.value.copy(pupillaryDistance = value) }

    fun addToCart() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                val prescriptionData = if (_state.value.showPrescriptionForm) {
                    com.opticalshop.data.model.PrescriptionData(
                        sphere = _state.value.sphere,
                        cylinder = _state.value.cylinder,
                        axis = _state.value.axis,
                        add = _state.value.add,
                        pupillaryDistance = _state.value.pupillaryDistance
                    )
                } else null

                val lensOptions = if (_state.value.lensType.isNotBlank()) {
                    com.opticalshop.data.model.LensOptions(
                        type = _state.value.lensType,
                        material = _state.value.lensMaterial,
                        coating = _state.value.lensCoating
                    )
                } else null

                val cartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    productImageUrl = if (product.images.isNotEmpty()) product.images[0] else "",
                    price = product.price,
                    quantity = _state.value.quantity,
                    prescriptionData = prescriptionData,
                    lensOptions = lensOptions
                )
                addToCartUseCase(user.id, cartItem)
            }
        }
    }
}
