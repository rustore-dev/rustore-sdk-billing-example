package ru.rustore.sdk.billingexample.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.product.Product
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult
import ru.rustore.sdk.billingclient.model.purchase.Purchase
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState
import ru.rustore.sdk.billingexample.R
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
    val state: StateFlow<BillingState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<BillingEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event = _event.asSharedFlow()

    init {
        updateProducts()
    }

    fun onProductClick(product: Product) {
        purchaseProduct(product)
    }

    fun updateProducts() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val products = billingClient.products.getProducts(availableProductIds).await()
                    val purchases = billingClient.purchases.getPurchases().await()
                    products to purchases
                }
            }.onSuccess { (products, purchases) ->
                proceedUnfinishedPurchases(purchases)
                updateProductList(products, purchases)
            }.onFailure(::handleError)
        }
    }

    private fun proceedUnfinishedPurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            if (purchase.developerPayload?.isNotEmpty() == true) {
                Log.w(
                    "RuStoreBillingClient",
                    "DeveloperPayloadInfo: ${purchase.developerPayload}"
                )
            }

            viewModelScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        val purchaseId = purchase.purchaseId ?: return@withContext
                        when (purchase.purchaseState) {
                            PurchaseState.PAID -> {
                                // If you can not give product to the customer use
                                // billingClient.purchases.deletePurchase(purchaseId).await()

                                billingClient.purchases.confirmPurchase(purchaseId).await()
                            }

                            else -> Unit
                        }
                    }
                }.onFailure(::handleError)
            }
        }
    }

    private fun updateProductList(products: List<Product>, purchases: List<Purchase>) {
        val nonBoughtProducts = products.filter { product ->
            purchases.none { product.productId == it.productId }
        }

        _state.update { currentState ->
            currentState.copy(
                products = nonBoughtProducts,
                isLoading = false
            )
        }
    }

    private fun handleError(throwable: Throwable) {
        _event.tryEmit(BillingEvent.ShowError(throwable))
        _state.update { it.copy(isLoading = false) }
    }

    private fun purchaseProduct(product: Product) {
        val developerPayload = "your_developer_payload"

        billingClient.purchases.purchaseProduct(
            productId = product.productId,
            developerPayload = developerPayload
        )
            .addOnSuccessListener { paymentResult ->
                handlePaymentResult(paymentResult, developerPayload)
            }
            .addOnFailureListener {
                setErrorStateOnFailure(it)
            }
    }

    private fun handlePaymentResult(paymentResult: PaymentResult, developerPayload: String) {
        when (paymentResult) {
            is PaymentResult.Success -> {
                confirmPurchase(
                    purchaseId = paymentResult.purchaseId,
                    developerPayload = developerPayload,
                )
            }

            else -> Unit
        }
    }

    private fun confirmPurchase(purchaseId: String, developerPayload: String) {
        _state.update { currentState ->
            currentState.copy(
                isLoading = true,
                snackbarResId = R.string.billing_purchase_confirm_in_progress
            )
        }

        billingClient.purchases.confirmPurchase(purchaseId, developerPayload)
            .addOnSuccessListener { proceedSuccessConfirmation(it) }
            .addOnFailureListener { setErrorStateOnFailure(it) }
    }

    private fun proceedSuccessConfirmation(response: Unit) {
        _event.tryEmit(
            BillingEvent.ShowDialog(
                InfoDialogState(
                    titleRes = R.string.billing_product_confirmed,
                    message = response.toString(),
                )
            )
        )
        _state.update { currentState ->
            currentState.copy(isLoading = false, snackbarResId = null)
        }
    }

    private fun setErrorStateOnFailure(error: Throwable) {
        _event.tryEmit(BillingEvent.ShowError(error))
        _state.value = _state.value.copy(isLoading = false)
    }
}
