package ru.rustore.sdk.billingexample.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rustore.sdk.billingexample.R
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.product.Product
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState
import ru.rustore.sdk.billingexample.di.PaymentsModule
import ru.rustore.sdk.billingexample.payment.model.BillingEvent
import ru.rustore.sdk.billingexample.payment.model.BillingState
import ru.rustore.sdk.billingexample.payment.model.InfoDialogState

class BillingExampleViewModel : ViewModel() {
    
    private val billingClient: RuStoreBillingClient = PaymentsModule.provideRuStoreBillingClient()

    private val availableProductIds = listOf(
        "productId1",
        "productId2",
        "productId3"
    )

    private val _state = MutableStateFlow(BillingState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<BillingEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event = _event.asSharedFlow()

    init {
        getProducts()
    }

    fun onProductClick(product: Product) {
        purchaseProduct(product)
    }

    fun getProducts() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val products = billingClient.products.getProducts(
                        productIds = availableProductIds
                    ).await()

                    val purchases = billingClient.purchases.getPurchases().await()

                    purchases.forEach { purchase ->
                        val purchaseId = purchase.purchaseId
                        if (purchaseId != null) {
                            when (purchase.purchaseState) {
                                PurchaseState.CREATED, PurchaseState.INVOICE_CREATED -> {
                                    billingClient.purchases.deletePurchase(purchaseId).await()
                                }

                                PurchaseState.PAID -> {
                                    billingClient.purchases.confirmPurchase(purchaseId).await()
                                }

                                else -> Unit
                            }
                        }
                    }

                    val nonBoughtProducts = products.filter { product ->
                        purchases.none { product.productId == it.productId }
                    }

                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            products = nonBoughtProducts,
                            isLoading = false
                        )
                    }
                }
            }.onFailure { throwable ->
                _event.tryEmit(BillingEvent.ShowError(throwable))
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun purchaseProduct(product: Product) {
        billingClient.purchases.purchaseProduct(product.productId)
            .addOnSuccessListener { paymentResult ->
                handlePaymentResult(paymentResult)
            }
            .addOnFailureListener {
                setErrorStateOnFailure(it)
            }
    }

    private fun handlePaymentResult(paymentResult: PaymentResult) {
        when (paymentResult) {
            is PaymentResult.Failure -> {
                paymentResult.purchaseId?.let { deletePurchase(it) }
            }

            is PaymentResult.Success -> {
                confirmPurchase(paymentResult.purchaseId)
            }

            else -> Unit
        }
    }

    private fun confirmPurchase(purchaseId: String) {
        _state.value = _state.value.copy(
            isLoading = true,
            snackbarResId = R.string.billing_purchase_confirm_in_progress
        )
        billingClient.purchases.confirmPurchase(purchaseId, null)
            .addOnSuccessListener { response ->
                _event.tryEmit(
                    BillingEvent.ShowDialog(
                        InfoDialogState(
                        titleRes = R.string.billing_product_confirmed,
                        message = response.toString(),
                    )
                    )
                )
                _state.value = _state.value.copy(
                    isLoading = false,
                    snackbarResId = null
                )
            }
            .addOnFailureListener {
                setErrorStateOnFailure(it)
            }
    }

    private fun deletePurchase(purchaseId: String) {
        _state.value = _state.value.copy(
            isLoading = true,
            snackbarResId = R.string.billing_purchase_delete_in_progress
        )
        billingClient.purchases.deletePurchase(purchaseId)
            .addOnSuccessListener { response ->
                _event.tryEmit(
                    BillingEvent.ShowDialog(InfoDialogState(
                        titleRes = R.string.billing_product_deleted,
                        message = response.toString()
                    ))
                )
                _state.value = _state.value.copy(isLoading = false)
            }
            .addOnFailureListener {
                setErrorStateOnFailure(it)
            }
    }

    private fun setErrorStateOnFailure(error: Throwable) {
        _event.tryEmit(BillingEvent.ShowError(error))
        _state.value = _state.value.copy(isLoading = false)
    }
}
