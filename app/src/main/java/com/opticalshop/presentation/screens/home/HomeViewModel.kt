package com.opticalshop.presentation.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.CartItem
import com.opticalshop.data.model.Category
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.cart.AddToCartUseCase
import com.opticalshop.domain.usecase.product.GetCategoriesUseCase
import com.opticalshop.domain.usecase.product.GetProductsUseCase
import com.opticalshop.data.model.Product
import com.opticalshop.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        getHomeData()
    }

    private fun getHomeData() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            _state.value = _state.value.copy(
                userName = user?.name ?: "Guest",
                profileImageUrl = user?.photoUrl
            )

            if (user != null) {
                launch {
                    userRepository.getWishlist(user.id).collect { result ->
                        if (result is Result.Success) {
                            _state.value = _state.value.copy(
                                wishlistProductIds = result.data.map { it.id }.toSet()
                            )
                        }
                    }
                }
            }

            combine(
                getProductsUseCase(),
                getCategoriesUseCase()
            ) { productsResult, categoriesResult ->
                val isLoading = productsResult is Result.Loading || categoriesResult is Result.Loading
                val error = when {
                    productsResult is Result.Error -> productsResult.exception.message
                    categoriesResult is Result.Error -> categoriesResult.exception.message
                    else -> null
                }

                val products = if (productsResult is Result.Success) productsResult.data else emptyList()
                val categories = if (categoriesResult is Result.Success) categoriesResult.data else emptyList()

                // Inject an "All" category if it doesn't exist
                val finalCategories = if (categories.none { it.id == "all" }) {
                    listOf(Category(id = "all", name = "All")) + categories
                } else categories

                val allProducts = products // Keep reference to all products
                
                _state.value.copy(
                    categories = finalCategories,
                    featuredProducts = products.filter { it.featured },
                    allProducts = allProducts,
                    popularProducts = allProducts, 
                    isLoading = isLoading,
                    error = error
                )
            }.collect { newState ->
                _state.value = newState
                filterProducts()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        filterProducts()
    }

    fun onCategorySelect(categoryId: String) {
        _state.value = _state.value.copy(selectedCategoryId = categoryId)
        filterProducts()
    }

    private fun filterProducts() {
        val query = _state.value.searchQuery.trim().lowercase()
        val categoryId = _state.value.selectedCategoryId
        
        val filtered = _state.value.allProducts.filter { product ->
            val matchesSearch = product.name.lowercase().contains(query) || product.description.lowercase().contains(query)
            val matchesCategory = categoryId == "all" || product.category == categoryId
            matchesSearch && matchesCategory
        }
        
        _state.value = _state.value.copy(popularProducts = filtered)
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                val cartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    productImageUrl = if (product.images.isNotEmpty()) product.images[0] else "",
                    price = product.price,
                    quantity = 1
                )
                addToCartUseCase(user.id, cartItem)
            }
        }
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                if (_state.value.wishlistProductIds.contains(product.id)) {
                    userRepository.removeFromWishlist(user.id, product.id)
                } else {
                    userRepository.addToWishlist(user.id, product)
                }
            }
        }
    }
}
