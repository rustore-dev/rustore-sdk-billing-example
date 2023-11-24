# RuStore SDK payments implementation

## Requirements

For the SDK to work correctly, the following requirements must be met:

- consoleApplicationId in create() is set correctly:
```
val billingClient = RuStoreBillingClientFactory.create(
    context = context,
    consoleApplicationId = "111111", // Replace with your id (https://console.rustore.ru/apps/111111)
    deeplinkScheme = "yourappscheme", // Must be the same as <data android:scheme="" />
)
```

- ApplicationId specified in build.gradle must be the same as applicationId in the apk file that you published in RuStore Console:
```
android {
    defaultConfig {
        applicationId = "ru.rustore.sdk.billingexample" // In buildTypes .debug is often added
    }
}
```

- The keystore signature must be the same as the signature used to sign the app published in RuStore Console. Make sure that the buildType used (e.g. debug) use the same signature as the published app (e.g. release).

## Consumption and cancellation

RuStore Billing SDK requires correct purchase state processing to provide for the best use case scenario.
Purchased consumable products must be consumed, while unfinished purchases have to be canceled—to make a new purchase possible.

Processing of unfinished purchases must be done by the app developer.

Use deletePurchase method if:

* The `getPurchases` method returned an error with the following status: 
 * `PurchaseState.CREATED`. 
 * `PurchaseState.INVOICE_CREATED`. 
* The `purchaseProduct` method returned `PaymentResult.Cancelled`. 
* The `purchaseProduct` method returned `PaymentResult.Failure`.

Use confirmPurchase if `getPurchases` returned a CONSUMABLE type error with PurchaseState.PAID status.


On opening your products screen get the purchases list with getPurchases() and process these purchases in the following manner:
```
val purchases = billingClient.purchases.getPurchases().await().purchases.orEmpty()
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
```
The example is taken from [BillingExampleViewModel.kt](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=app%2Fsrc%2Fmain%2Fkotlin%2Fru%2Frustore%2Fsdk%2Fbillingexample%2Fpayment%2FBillingExampleViewModel.kt).
> Synchronous await() methods are optional.

Process the purchase result in the following manner:
```
private fun purchaseProduct(product: Product, orderId: String?, quantity: Int, developerPayload: String) {
    billingClient.purchases.purchaseProduct(product.productId)
        .addOnSuccessListener { paymentResult ->
            handlePaymentResult(paymentResult, product)
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

```
The example is taken from [BillingExampleViewModel.kt](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=app%2Fsrc%2Fmain%2Fkotlin%2Fru%2Frustore%2Fsdk%2Fbillingexample%2Fpayment%2FBillingExampleViewModel.kt).

## Still have questions left?
If there are any questions about SDK payments integration, please, follow this link:
[https://help.rustore.ru/rustore/trouble/user/help_user_email](https://help.rustore.ru/rustore/trouble/user/help_user_email)
or write to support@rustore.ru.