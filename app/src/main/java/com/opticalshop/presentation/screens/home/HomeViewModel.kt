package com.opticalshop.presentation.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.domain.model.Result
import com.opticalshop.domain.usecase.product.GetCategoriesUseCase
import com.opticalshop.domain.usecase.product.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        getHomeData()
    }

    fun getHomeData() {
        viewModelScope.launch {
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

                HomeState(
                    categories = categories,
                    featuredProducts = products.filter { it.featured },
                    isLoading = isLoading,
                    error = error
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
}
