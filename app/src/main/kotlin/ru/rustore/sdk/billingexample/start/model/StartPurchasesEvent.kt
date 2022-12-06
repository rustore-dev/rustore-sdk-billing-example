package ru.rustore.sdk.billingexample.start.model

import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult

sealed class StartPurchasesEvent {
    data class PurchasesAvailability(val availability: FeatureAvailabilityResult) : StartPurchasesEvent()
    data class Error(val throwable: Throwable): StartPurchasesEvent()
}
