package ru.rustore.sdk.billingexample.payment.model

import androidx.annotation.StringRes

data class InfoDialogState(
    @StringRes val titleRes: Int,
    val message: String
)
