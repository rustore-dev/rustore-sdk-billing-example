# Пример внедрения SDK платежей RuStore
## [Документация SDK платежей](https://help.rustore.ru/rustore/for_developers/developer-documentation/sdk_payments/SDK-connecting-payments/quick_start)

### [English version of README.md](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=README_EN.MD&branch=master)


### Требуемые условия

Для корректной работы SDK необходимо соблюдать следующие условия:

- Задан правильно consoleApplicationId в create():
```
val billingClient = RuStoreBillingClientFactory.create(
    context = context,
    consoleApplicationId = "111111", // Заменить на свой id (https://console.rustore.ru/apps/111111)
    deeplinkScheme = "yourappscheme", // Должен совпадать с <data android:scheme="" />
)
```

- ApplicationId, указанный в build.gradle, совпадает с applicationId apk-файла, который вы публиковали в консоль RuStore:
```
android {
    defaultConfig {
        applicationId = "ru.rustore.sdk.billingexample" // Зачастую в buildTypes приписывается .debug
    }
}
```

- Подпись keystore должна совпадать с подписью, которой было подписано приложение, опубликованное в консоль RuStore. Убедитесь, что используемый buildType (пр. debug) использует такую же подпись, что и опубликованное приложение (пр. release).

### Потребление и отмена покупки
RuStore Billing SDK требует правильно обрабатывать состояния покупки, чтобы предоставить наилучший сценарий использования.
Так, купленные потребляемые товары необходимо потребить, а незаконченные покупки - отменять, чтобы иметь возможность заново начать новую.
[Подробнее о потреблении и отмене.](https://help.rustore.ru/rustore/for_developers/developer-documentation/sdk_payments/SDK-connecting-payments/%20consumption-and-withdrawal)

При открытии вашего экрана товаров, необходимо получить список товаров с помощью getPurchases() и обработать товары следующим образом:
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
Пример взят из [BillingExampleViewModel.kt](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=app%2Fsrc%2Fmain%2Fkotlin%2Fru%2Frustore%2Fsdk%2Fbillingexample%2Fpayment%2FBillingExampleViewModel.kt).
> Использовать синхронные await() методы не обязательно.

Обработать результат покупки необходимо следующим образом:
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
Пример взят из [BillingExampleViewModel.kt](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=app%2Fsrc%2Fmain%2Fkotlin%2Fru%2Frustore%2Fsdk%2Fbillingexample%2Fpayment%2FBillingExampleViewModel.kt).

### Есть вопросы
Если появились вопросы по интеграции SDK платежей, обратитесь по этой ссылке:
[https://help.rustore.ru/rustore/trouble/user/help_user_email](https://help.rustore.ru/rustore/trouble/user/help_user_email)
или напишите на почту support@rustore.ru.