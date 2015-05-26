#PayPoint Advanced Payments SDK

Requires at minimum Android 4.0 (API level 14)

**NOTE this step will change once the library is available as a Maven artifact**

Copy the PayPoint SDK library paypoint_sdk-x.x.x.aar into the module libs folder.

Add the following to your module gradle build

```groovy
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compile(name:'paypoint_sdk-x.x.x', ext:'aar')

    compile 'com.squareup.retrofit:retrofit:1.9.0'
    compile 'com.google.code.gson:gson:2.2.4'
    compile 'com.squareup.okhttp:okhttp-urlconnection:2.0.0'
    compile 'com.squareup.okhttp:okhttp:2.0.0'
}
```

In the module gradle build set minSdkVersion to 14 or above.

Add the following to your AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<activity android:name="com.paypoint.sdk.library.ThreeDSActivity"
    android:screenOrientation="portrait">
</activity>
```

##Making a Payment

Create a simple activity accepting a card number, expiry and CV2.
Get an instance of PaymentManager in onCreate()

```java
paymentManager = PaymentManager.getInstance(this)
        .setUrl(URL_PAYPOINT);
```

Use EndpointManager.getEndpointUrl() to get the URL for a PayPoint environment.

Register a payment callback handler in OnResume and unregister the callback in OnPause to ensure your activity handles device orientation changes correctly if not locked to a single orientation.

```java
@Override
protected void onPause() {
    super.onPause();

    paymentManager.lockCallback();
    paymentManager.unregisterPaymentCallback();
}

@Override
protected void onResume() {
    super.onResume();

    paymentManager.registerPaymentCallback(this);
    paymentManager.unlockCallback();
}
```

In your payment button handler build a PaymentRequest

```java
PaymentCard card = new PaymentCard()
        .setPan(cardNumber)
        .setExpiryDate(cardExpiry)
        .setCv2(cardCv2);

Transaction transaction = new Transaction()
        .setCurrency(currency) // e.g. “GBP”
        .setAmount(amount)
        .setMerchantReference(merchantRef); // up to merchant to create a unique merchantRef

// create the payment request
PaymentRequest request = new PaymentRequest()
        .setCard(card)
        .setTransaction(transaction);
```

To authorise a payment call setDeferred(true) on the transaction.

The card holder address, financial services data and customer details can also optionally be created and set on the request.

Your activity will need to implement the PaymentManager.MakePaymentCallback interface.

Validate the payment details handling the PaymentValidationException

```java
paymentManager.validatePaymentDetails(request);
```

Note: the PaymentManager also provides static functions for inline validation of the card fields as they are being entered

```java
public static void validatePan(String pan) throws PaymentValidationException

public static void validateExpiry(String expiryDate) throws PaymentValidationException

public static void validateCv2(String cv2) throws PaymentValidationException
```

PaymentValidationException holds an error code enumeration describing the error.

If the PaymentRequest validates successfully i.e. does not throw a PaymentValidationException, your app should then communicate with your server to request a PayPoint authorisation token. This token, when returned, should be used to create a PayPointCredentials object which should then be passed to the PaymentManager

```java
PayPointCredentials credentials = new PayPointCredentials()
        .setInstallationId((<YOUR_INSTALLATION_ID>);)
        .setToken(token);

paymentManager.setCredentials(credentials);
```

Next, make the payment by calling makePayment() on the PaymentManager passing the request

```java
paymentManager.makePayment(request);
```

This call to makePayment() will callback to your app when completed in one of the following functions

```java
public void paymentSucceeded(PaymentSuccess paymentSuccess)

public void paymentFailed(PaymentError paymentError)
```

PaymentSuccess - has accessors for transaction id, merchant reference, amount, currency and last four digits of the card number.
PaymentError – use getKind() to return the type of error. PayPoint errors contain a reasonCode and reasonMessage which can be used to feedback to the user

```java
if (paymentError.getKind() == PaymentError.Kind.PAYPOINT) {
    String reasonMessage = paymentError.getPayPointError().getReasonMessage();
    PaymentError.ReasonCode reasonCode = paymentError.getPayPointError().getReasonCode();
} else if (paymentError.getKind() == PaymentError.Kind.NETWORK) {
    // network error
}
```






