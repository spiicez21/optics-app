package com.opticalshop.presentation.screens.product

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.CartItem
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(ProductDetailState())
    val state: State<ProductDetailState> = _state

    init {
        savedStateHandle.get<String>("productId")?.let { productId ->
            getProduct(productId)
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

    fun addToCart() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                val cartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    productImageUrl = if (product.images.isNotEmpty()) product.images[0] else "",
                    price = product.price,
                    quantity = _state.value.quantity
                )
                addToCartUseCase(user.id, cartItem)
            }
        }
    }
}
