package com.opticalshop.presentation.screens.wishlist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.Product
import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import com.opticalshop.data.model.CartItem
import com.opticalshop.domain.usecase.cart.AddToCartUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {

    private val _state = mutableStateOf(WishlistState())
    val state: State<WishlistState> = _state

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                user?.let {
                    userRepository.getWishlist(it.id).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _state.value = _state.value.copy(
                                    products = result.data,
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
    }

    fun removeFromWishlist(productId: String) {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                user?.let {
                    userRepository.removeFromWishlist(it.id, productId)
                }
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                user?.let {
                    val cartItem = CartItem(
                        productId = product.id,
                        productName = product.name,
                        productImageUrl = if (product.images.isNotEmpty()) product.images[0] else "",
                        price = product.price,
                        quantity = 1
                    )
                    addToCartUseCase(it.id, cartItem)
                }
            }
        }
    }
}
