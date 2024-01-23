# Пример внедрения SDK платежей RuStore
## [Документация SDK платежей](https://www.rustore.ru/help/sdk/payments/general)

### [English version of README.md](https://gitflic.ru/project/rustore/rustore-sdk-billing-example/blob?file=README_EN.MD&branch=master)


### Оглавление
- [Условия работы платежей](#Условия-работы-платежей)
- [Подготовка требуемых параметров](#Подготовка-требуемых-параметров)
- [Настройка примера приложения](#Настройка-примера-приложения)
- [Сценарий использования](#Сценарий-использования)
- [Обработка deeplink](#Обработка-deeplink)
- [Условия распространения](#Условия-распространения)
- [Техническая поддержка](#Техническая-поддержка)


### Условия работы платежей
Для работы проведения платежей необходимо соблюдение следующих условий.

1. На устройстве пользователя установлено приложение RuStore.

2. Пользователь авторизован в приложении RuStore.

3. Пользователь и приложение не должны быть заблокированы в RuStore.

4. Для приложения включена возможность покупок в системе [RuStore Консоль](https://console.rustore.ru/sign-in).

> Сервис имеет некоторые ограничения на работу за пределами РФ.


### Подготовка требуемых параметров
Для корректной настройки примера приложения вам следует подготовить:

1. `consoleApplicationId` - код приложения из консоли разработчика RuStore (пример: https://console.rustore.ru/apps/123456), тут `consoleApplicationId` = 123456

2. `applicationId` - из приложения, которое вы публиковали в консоль RuStore, находится в файле build.gradle вашего проекта

   ```
    android {
       defaultConfig {
       applicationId = "ru.rustore.sdk.billingexample" 
       }
    }
   ```

3. `availableProductIds` - [подписки](https://www.rustore.ru/help/developers/monetization/create-app-subscription/) и [разовые покупки](https://www.rustore.ru/help/developers/monetization/create-paid-product-in-application/) доступные в вашем приложении

4. `release.keystore` - подпись, которой было подписано приложение, опубликованное в консоль RuStore.

5. `release.properties` - в этом файле должны быть указаны параметры подписи, которой было подписано приложение, опубликованное в консоль RuStore. [Как работать с ключами подписи APK-файлов](https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/apk-signature/)


###  Настройка примера приложения
Для проверки работы приложения вы можете воспользоваться функционалом [тестовых платежей](https://www.rustore.ru/help/developers/monetization/sandbox).

1. Указать `consoleApplicationId` своего приложения в `RuStoreBillingClientFactory.create()`:

   ```
   val billingClient = RuStoreBillingClientFactory.create(
       context = context,
       consoleApplicationId = "111111", // Заменить на свой id (https://console.rustore.ru/apps/111111)
   ) 
   ```

2. Замените `applicationId`, в файле build.gradle, на applicationId apk-файла, который вы публиковали в консоль RuStore:

   ```
   android {
       defaultConfig {
          applicationId = "ru.rustore.sdk.billingexample" // Зачастую в buildTypes приписывается .debug
       }
   }
   ```

3. В директории `cert` замените сертификат `release.keystore` - сертификатом своего приложения, так же в `release.properties` выполните настройку параметров `key_alias`, `key_password`, `store_password`.  Подпись `release.keystore` должна совпадать с подписью, которой было подписано приложение, опубликованное в консоль RuStore. Убедитесь, что используемый `buildType` (пр. debug) использует такую же подпись, что и опубликованное приложение (пр. release).

4. В `BillingExampleViewModel` в переменной `availableProductIds` перечислите подписки и разовые продукты доступные в вашем приложении:

   ```
   private val availableProductIds = listOf(
       "productId1",
       "productId2",
       "productId3"
   )
   ```

5. Запустите проект и проверьте работу приложения


### Сценарий использования

#### Проверка доступности работы с платежами
Нажатие на кнопку "Начать покупки" выполняет [проверку доступности платежей](https://www.rustore.ru/help/sdk/payments/checkpurchasesavailability/),


![Проверка доступности работы с платежами](app/src/main/res/drawable/start.png)

#### Получение списка продуктов
При открытии экрана `BillingExampleFragment` выполняется получение и отображение [списка продуктов](https://www.rustore.ru/help/sdk/payments/getproducts/)


![Получение списка продуктов](app/src/main/res/drawable/get_products.png)

#### Покупка продукта
Нажатие на айтем выполняет запуск сценария [покупки продукта](https://www.rustore.ru/help/sdk/payments/purchaseproduct/) с отображением шторки выбора метода оплаты.
Далее если приходит ответ PaymentResult.Success -> выполняется [подтверждение покупки](https://www.rustore.ru/help/sdk/payments/confirmpurchase/), если приходит ответ PaymentResult.Failure -> выполняется [отмена покупки](https://www.rustore.ru/help/sdk/payments/deletepurchase/).



![Покупка продукта](app/src/main/res/drawable/purchase.png)


### Обработка deeplink
Данный пункт не относится к настройке примера тестового приложения. Он информирует о том, как правильно настроить `deeplink` в вашем приложении

`deeplinkScheme` — схема deeplink, необходимая для возврата в ваше приложение после оплаты через стороннее приложение (например, SberPay или СБП). SDK генерирует свой хост к данной схеме.

Для корректной работы оплаты через сторонние приложения (СБП или SberPay), вам необходимо правильно реализовать обработку deeplink. Для этого необходимо указать в `AndroidManifest.xml` intent-filter с scheme вашего проекта.

1. В файле `AndroidManifest.xml` в параметре data android:scheme укажите URL-адрес для использования deeplink (должен совпадать с параметром `deeplinkScheme` из пункта 1)
 
   ```
   <activity
   android:name=".MainActivity">
   
       <intent-filter>
          <action android:name="android.intent.action.VIEW" />
          <data android:scheme="rustoresdkexamplescheme" /> // Заменить на свой deeplink
       </intent-filter>

   </activity>
   ```

2. Указать `deeplinkScheme` своего приложения в `RuStoreBillingClientFactory.create()`:

   ```
   val billingClient = RuStoreBillingClientFactory.create(
       context = context,
       deeplinkScheme = "rustoresdkexamplescheme", // Укажите URL-адрес для использования deeplink. Должен совпадать с <data android:scheme="" />
   )
   ```

3. Далее в Activity, в которую необходимо вернуться после совершения оплаты (ваша страница магазина), нужно добавить:

   ```
   class MainActivity: AppCompatActivity() {

       // Previously created with RuStoreBillingClientFactory.create()
       private val billingClient: RuStoreBillingClient = YourDependencyInjection.getBillingClient()

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
   ```

4. Для восстановления состояния вашего приложения при возврате с deeplink добавьте в `AndroidManifest.xml` android:launchMode="singleTask"

   ```
   <activity
      android:name=".MainActivity"
      android:launchMode="singleTask"
      android:exported="true"
      android:screenOrientation="portrait"
      android:windowSoftInputMode="adjustResize">
   ```


### Условия распространения
Данное программное обеспечение, включая исходные коды, бинарные библиотеки и другие файлы распространяется под лицензией MIT. Информация о лицензировании доступна в документе `MIT-LICENSE.txt`


### Техническая поддержка
Если появились вопросы по интеграции SDK платежей, обратитесь по этой [ссылке](https://www.rustore.ru/help/) или напишите на почту support@rustore.ru.