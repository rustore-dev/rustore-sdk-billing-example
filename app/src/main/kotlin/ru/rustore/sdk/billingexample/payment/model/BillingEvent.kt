package ru.rustore.sdk.billingexample.payment.model

sealed class BillingEvent {
    data class ShowDialog(val dialogInfo: InfoDialogState): BillingEvent()
    data class ShowError(val error: Throwable): BillingEvent()
}
