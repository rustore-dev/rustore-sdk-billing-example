package ru.rustore.sdk.billingexample.start

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.utils.pub.checkPurchasesAvailability
import ru.rustore.sdk.billingexample.start.model.StartPurchasesEvent
import ru.rustore.sdk.billingexample.start.model.StartPurchasesState

class StartPurchasesViewModel: ViewModel() {

    private val _state = MutableStateFlow(StartPurchasesState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<StartPurchasesEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event = _event.asSharedFlow()

    fun checkPurchasesAvailability(context: Context) {
        _state.value = _state.value.copy(isLoading = true)
        RuStoreBillingClient.checkPurchasesAvailability(context)
            .addOnSuccessListener { result ->
                _state.value = _state.value.copy(isLoading = false)
                _event.tryEmit(StartPurchasesEvent.PurchasesAvailability(result))
            }
            .addOnFailureListener { throwable ->
                _state.value = _state.value.copy(isLoading = false)
                _event.tryEmit(StartPurchasesEvent.Error(throwable))
            }
    }
}
