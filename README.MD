# Пример внедрения SDK платежей RuStore
## [Документация SDK платежей](https://help.rustore.ru/rustore/for_developers/developer-documentation/SDK-connecting-payments)

### Требуемые условия

Для корректной работы SDK необходимо соблюдать следующие условия:
1. Задан правильно consoleApplicationId в init():
```
RuStoreBillingClient.init(
    application = this,
    consoleApplicationId = "111111", // Заменить на свой id (https://console.rustore.ru/apps/111111)
    deeplinkPrefix = "yourappscheme://iamback",
)
```
2. ApplicationId, указанный в build.gradle, совпадает с applicationId apk-файла, который вы публиковали в консоль RuStore:
```
android {
    defaultConfig {
        applicationId = "ru.rustore.sdk.billingexample" // Зачастую в buildTypes приписывается .debug
    }
}
```
3. Подпись keystore должна совпадать с подписью, которой было подписано приложение, опубликованное в консоль RuStore. Убедитесь, что используемый buildType (пр. debug) использует такую же подпись, что и опубликованное приложение (пр. release).

### Потребление и отмена покупки
RuStore Billing SDK требует правильно обрабатывать состояния покупки, чтобы предоставить наилучший сценарий использования.
Так, купленные потребляемые товары необходимо потребить, а незаконченные покупки - отменять, чтобы иметь возможность заново начать новую.
[Подробнее о потреблении и отмене.](https://help.rustore.ru/rustore/for_developers/developer-documentation/SDK-connecting-payments/%20consumption-and-withdrawal)

При открытии вашего экрана товаров, необходимо получить список товаров с помощью getPurchases() и обработать товары следующим образом:
```
val purchases = RuStoreBillingClient.purchases.getPurchases().await().purchases.orEmpty()
purchases.forEach { purchase ->
    val purchaseId = purchase.purchaseId
    if (purchaseId != null) {
         when (purchase.purchaseState) {
                PurchaseState.CREATED, PurchaseState.INVOICE_CREATED -> {
                    RuStoreBillingClient.purchases.deletePurchase(purchaseId).await()
                }
                PurchaseState.PAID -> {
                    RuStoreBillingClient.purchases.confirmPurchase(purchaseId).await()
                }
                else -> Unit
        }
    }
}
```
Пример взят из [BillingExampleViewModel.kt](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=app%2Fsrc%2Fmain%2Fkotlin%2Fru%2Frustore%2Fsdk%2Fbillingexample%2Fpayment%2FBillingExampleViewModel.kt).
> Использовать синхронные await() методы не обязательно.

Обработать результат покупки необходимо следующим образом:
```
private fun purchaseProduct(product: Product) {
        RuStoreBillingClient.purchases.purchaseProduct(product.productId)
            .addOnSuccessListener { paymentResult ->
                handlePaymentResult(paymentResult, product)
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    private fun handlePaymentResult(paymentResult: PaymentResult, product: Product) {
        when (paymentResult) {
            is PaymentResult.InvalidPurchase -> {
                paymentResult.purchaseId?.let { deletePurchase(it) }
            }
            is PaymentResult.PurchaseResult -> {
                when (paymentResult.finishCode) {
                    PaymentFinishCode.SUCCESSFUL_PAYMENT -> {
                        if (product.productType == ProductType.CONSUMABLE) {
                            confirmPurchase(paymentResult.purchaseId)
                        }
                    }
                    PaymentFinishCode.CLOSED_BY_USER,
                    PaymentFinishCode.UNHANDLED_FORM_ERROR,
                    PaymentFinishCode.PAYMENT_TIMEOUT,
                    PaymentFinishCode.DECLINED_BY_SERVER,
                    PaymentFinishCode.RESULT_UNKNOWN,
                    -> {
                        deletePurchase(paymentResult.purchaseId)
                    }
                }
            }
            else -> Unit
        }
    }
```
Пример взят из [BillingExampleViewModel.kt](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=app%2Fsrc%2Fmain%2Fkotlin%2Fru%2Frustore%2Fsdk%2Fbillingexample%2Fpayment%2FBillingExampleViewModel.kt).