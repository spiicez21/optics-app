package com.opticalshop.presentation.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.CartItem
import com.opticalshop.data.model.Category
import com.opticalshop.data.model.Product
import com.opticalshop.data.model.User
import com.opticalshop.data.repository.UserRepository
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.auth.GetCurrentUserUseCase
import com.opticalshop.domain.usecase.cart.AddToCartUseCase
import com.opticalshop.domain.usecase.product.GetCategoriesUseCase
import com.opticalshop.domain.usecase.product.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
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

    /** Cached after the first auth resolution; avoids a `.first()` call per action. */
    private var cachedUser: User? = null

    private var fetchJob: kotlinx.coroutines.Job? = null

    /** One-shot event: navigate to cart screen after a successful add-to-cart. */
    private val _cartNavigationEvent = Channel<Unit>(Channel.BUFFERED)
    val cartNavigationEvent = _cartNavigationEvent.receiveAsFlow()

    init {
        observeUser()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        getHomeData()
    }

    /**
     * Observes the auth state once. When we have a user, starts persistent
     * profile/wishlist listeners and loads home data.
     */
    private fun observeUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            cachedUser = user
            _state.value = _state.value.copy(
                userName = user?.name?.ifBlank { "Guest" } ?: "Guest",
                profileImageUrl = user?.getProfileImageUrl()
            )

            if (user != null) {
                launch {
                    userRepository.getProfile(user.id).collect { result ->
                        if (result is Result.Success) {
                            _state.value = _state.value.copy(
                                userName = result.data.name.ifBlank { "Guest" },
                                profileImageUrl = result.data.getProfileImageUrl()
                            )
                        }
                    }
                }
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

            getHomeData()
        }
    }

    private fun getHomeData() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
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

                val finalCategories = if (categories.none { it.id == "all" }) {
                    listOf(Category(id = "all", name = "All")) + categories
                } else categories

                _state.value.copy(
                    categories = finalCategories,
                    featuredProducts = products.filter { it.featured },
                    allProducts = products,
                    popularProducts = if (_state.value.selectedCategoryId == "all" && _state.value.searchQuery.isBlank()) products else _state.value.popularProducts,
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
            val matchesSearch = product.name.lowercase().contains(query) ||
                product.description.lowercase().contains(query)
            val matchesCategory = categoryId == "all" || product.category == categoryId
            matchesSearch && matchesCategory
        }

        _state.value = _state.value.copy(popularProducts = filtered)
    }

    fun addToCart(product: Product) {
        val user = cachedUser ?: return
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id,
                productName = product.name,
                productImageUrl = if (product.images.isNotEmpty()) product.images[0] else "",
                price = product.price,
                quantity = 1
            )
            val result = addToCartUseCase(user.id, cartItem)
            if (result is com.opticalshop.domain.model.Result.Success) {
                _cartNavigationEvent.send(Unit)
            }
        }
    }

    fun toggleWishlist(product: Product) {
        val user = cachedUser ?: return
        viewModelScope.launch {
            if (_state.value.wishlistProductIds.contains(product.id)) {
                userRepository.removeFromWishlist(user.id, product.id)
            } else {
                userRepository.addToWishlist(user.id, product)
            }
        }
    }
}
