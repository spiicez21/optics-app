package com.opticalshop.presentation.screens.cart

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.cart.GetCartUseCase
import com.opticalshop.domain.usecase.cart.RemoveFromCartUseCase
import com.opticalshop.domain.usecase.cart.UpdateCartQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = mutableStateOf(CartState())
    val state: State<CartState> = _state

    init {
        getCart()
    }

    private fun getCart() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                getCartUseCase(user.id).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val cart = result.data
                            val total = cart.items.sumOf { it.price * it.quantity }
                            _state.value = _state.value.copy(
                                cartItems = cart.items,
                                totalAmount = total,
                                isLoading = false,
                                error = null
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

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null && quantity > 0) {
                updateCartQuantityUseCase(user.id, productId, quantity)
            }
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                removeFromCartUseCase(user.id, productId)
            }
        }
    }
}
