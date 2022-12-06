package ru.rustore.sdk.billingexample

import android.app.Application
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingexample.init.PaymentLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        RuStoreBillingClient.init(
            application = this,
            consoleApplicationId = "123456789",
            deeplinkPrefix = "rustoresdkexamplescheme://iamback",
            externalPaymentLogger = PaymentLogger(tag = "ExamplePaymentApp"),
            debugLogs = true
        )
    }
}
