package com.opticalshop.presentation.screens.orders

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.Order
import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.data.repository.OrderRepository
import com.opticalshop.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(OrdersState())
    val state: State<OrdersState> = _state

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                user?.let {
                    orderRepository.getOrderHistory(it.id).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _state.value = _state.value.copy(
                                    orders = result.data,
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
}
