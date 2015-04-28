#PayPoint Advanced Payments SDK

Android Studio (currently tested on 1.1.0)

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

Add the following permissions to your AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

##Making a Payment

Create a simple activity accepting a card number, expiry and CV2.
Create an instance of PaymentManager in onCreate()

```java
paymentManager = new PaymentManager(this)
        .setUrl(<PAYPOINT_URL>);
```

Use EndpointManager.getEndpointUrl() to get the URL for a PayPoint environment.

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
        .setCallback(this)
        .setCard(card)
        .setTransaction(transaction);
```

The card holder address can also optionally be created and passed into the request, create an instance of BillingAddress, call the setter methods and pass to the PaymentRequest.
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

```java
public enum ErrorCode {
    CARD_EXPIRED,                   // card has expired
    CARD_EXPIRY_INVALID,            // incorrect length or non numeric
    CARD_PAN_INVALID,               // incorrect PAN length or non numeric
    CARD_PAN_INVALID_LUHN,          // invalid card PAN or non numeric
    CARD_CV2_INVALID,               // incorrect CV2 length
    TRANSACTION_INVALID_AMOUNT,     // no transaction or negative amount specified
    TRANSACTION_INVALID_CURRENCY,   // no currency specified
    NETWORK_NO_CONNECTION,          // device has no network connection
    INVALID_CREDENTIALS,            // credentials missing (PayPoint token or installation id)
    INVALID_URL,                    // PayPoint server URL not passed in
    INVALID_REQUEST,                // empty PaymentRequest
    INVALID_TRANSACTION,            // empty Transaction
    INVALID_CARD                    // empty PaymentCard
}
```

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
public enum ReasonCode {
    UNKNOWN(-1),
    SUCCESS(0),                         // Operation successful as described
    INVALID(1),                         // Request was not correctly formed
    AUTHENTICATION_FAILED(2),           // The presented API token was not valid, or the wrong type of authentication was used
    CLIENT_TOKEN_EXPIRED(3),            // Get a new token
    UNAUTHORISED_REQUEST(4),            // The token was valid, but does not grant you access to use the specified feature
    TRANSACTION_FAILED_TO_PROCESS(5),   // The transaction was successfully submitted but failed to be processed correctly.
    SERVER_ERROR(6);                   	// An internal server error occurred at paypoint
}

if (paymentError.getKind() == PaymentError.Kind.PAYPOINT) {
    String reasonMessage = paymentError.getPayPointError().getReasonMessage();
    PaymentError.ReasonCode reasonCode = paymentError.getPayPointError().getReasonCode();
} else if (paymentError.getKind() == PaymentError.Kind.NETWORK) {
    // network error
}
```






