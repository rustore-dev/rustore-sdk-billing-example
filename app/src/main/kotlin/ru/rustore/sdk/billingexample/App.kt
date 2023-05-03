package ru.rustore.sdk.billingexample

import android.app.Application
import ru.rustore.sdk.billingexample.di.PaymentsModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        PaymentsModule.install(this)
    }
}
