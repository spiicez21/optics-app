package com.opticalshop.presentation.payment

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class RazorpayPaymentEvent {
    data class Success(val paymentId: String) : RazorpayPaymentEvent()
    data class Failure(val message: String?) : RazorpayPaymentEvent()
}

object RazorpayEventBus {
    private val _events = MutableSharedFlow<RazorpayPaymentEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<RazorpayPaymentEvent> = _events

    fun emit(event: RazorpayPaymentEvent) {
        _events.tryEmit(event)
    }
}
