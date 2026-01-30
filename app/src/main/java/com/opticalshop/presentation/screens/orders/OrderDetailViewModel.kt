package com.opticalshop.presentation.screens.orders

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opticalshop.data.model.Order
import com.opticalshop.data.repository.AuthRepository
import com.opticalshop.data.repository.OrderRepository
import com.opticalshop.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(OrderDetailState())
    val state: State<OrderDetailState> = _state

    init {
        savedStateHandle.get<String>("orderId")?.let { orderId ->
            getOrder(orderId)
        }
    }

    private fun getOrder(orderId: String) {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                user?.let {
                    orderRepository.getOrderById(it.id, orderId).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _state.value = _state.value.copy(
                                    order = result.data,
                                    isLoading = false
                                )
                            }
                            is Result.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.exception.message,
                                    isLoading = false
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
