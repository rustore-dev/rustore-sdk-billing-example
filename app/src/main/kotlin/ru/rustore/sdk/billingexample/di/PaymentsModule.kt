package ru.rustore.sdk.billingexample.di

import android.app.Application
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import ru.rustore.sdk.billingexample.init.PaymentLogger

/**
* Может быть любым DI, допустим Hilt или Koin.
* */
object PaymentsModule {
    private lateinit var ruStoreBillingClient: RuStoreBillingClient

    fun install(app: Application) {
        ruStoreBillingClient = RuStoreBillingClientFactory.create(
            context = app,
            consoleApplicationId = "123456789",
            deeplinkScheme = "rustoresdkexamplescheme",
            externalPaymentLoggerFactory = { PaymentLogger(tag = "ExamplePaymentApp") },
            debugLogs = true
        )
    }

    fun provideRuStoreBillingClient(): RuStoreBillingClient = ruStoreBillingClient
}
