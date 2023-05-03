package ru.rustore.sdk.billingexample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingexample.di.PaymentsModule

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val billingClient: RuStoreBillingClient = PaymentsModule.provideRuStoreBillingClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            billingClient.onNewIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        billingClient.onNewIntent(intent)
    }
}
