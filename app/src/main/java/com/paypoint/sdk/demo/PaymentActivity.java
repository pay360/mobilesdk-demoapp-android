package com.paypoint.sdk.demo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.paypoint.sdk.demo.utils.FontUtils;
import com.paypoint.sdk.demo.widget.CustomMessageDialog;
import com.paypoint.sdk.demo.widget.CustomWaitDialog;
import com.paypoint.sdk.library.exception.PaymentException;
import com.paypoint.sdk.library.payment.PaymentError;
import com.paypoint.sdk.library.payment.PaymentManager;
import com.paypoint.sdk.library.payment.PaymentRequest;
import com.paypoint.sdk.library.payment.request.BillingAddress;
import com.paypoint.sdk.library.payment.request.PaymentCard;
import com.paypoint.sdk.library.payment.request.Transaction;
import com.paypoint.sdk.library.security.PayPointCredentials;

import java.util.UUID;

public class PaymentActivity extends ActionBarActivity implements PaymentManager.MakePaymentCallback {

//    private static final String URL = "http://10.0.3.2:5000/mobileapi";       // Genymotion
    private static final String URL = "http://192.168.3.138:5000/mobileapi";    // Pete's machine

    private EditText editCardNumber;
    private EditText editCardExpiry;
    private EditText editCardCvv;
    private Button buttonPay;
    private CustomWaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        FontUtils.setFontForHierarchy(this, getWindow().getDecorView().findViewById(android.R.id.content));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.activity_payment_title);

        editCardNumber = (EditText)findViewById(R.id.editCardNumber);
        editCardExpiry = (EditText)findViewById(R.id.editCardExpiry);
        editCardCvv = (EditText)findViewById(R.id.editCardCVV);
        buttonPay = (Button)findViewById(R.id.buttonPay);

        editCardNumber.addTextChangedListener(new CardNumberFormatter());

        // TEST DETAILS START
        editCardNumber.setText("9900 0000 0000 5159");
        editCardExpiry.setText("1115");
        editCardCvv.setText("123");
        // TEXT DETAILS END

        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePayment();
            }
        });
    }

    private void makePayment() {

        String cardNumber = editCardNumber.getText().toString();
        String cardExpiry = editCardExpiry.getText().toString();
        String cardCvv = editCardCvv.getText().toString();

        // TODO call merchant server to get token

        // Instantiate the PaymentManager in the SDK
        PaymentManager paymentManager = new PaymentManager(this);

        // Build up the card payment
        PaymentCard card = new PaymentCard()
                .setPan(cardNumber)
                .setExpiryDate(cardExpiry)
                .setCv2(cardCvv);

        BillingAddress address = new BillingAddress()
                .setLine1("Flat1")
                .setLine2("Cauldron House")
                .setLine3("A Street")
                .setLine4("Twertonia")
                .setCity("Bath")
                .setRegion("Somerset")
                .setPostcode("BA1 234")
                .setCountryCode("GBR");

        Transaction transaction = new Transaction()
                .setCurrency("GBP")
                .setAmount(100.00f)
                .setMerchantReference("PP_" + UUID.randomUUID().toString().substring(0, 8)); // Generate this in your app in whichever way suits

        // Use test credentials
        PayPointCredentials credentials = new PayPointCredentials().setInstallationId("1212312")
                .setToken("VALID_TOKEN");

        // TODO add endpoint manager to get preconfigured environments
        //String url = EndpointManager.getUrl(MITE);

        // Make the payment handling any errors
        PaymentRequest request = new PaymentRequest()
                .setCallback(this)
                .setCard(card)
                .setAddress(address)
                .setTransaction(transaction)
                .setUrl(URL)
                .setCredentials(credentials);

        try {
            paymentManager.makePayment(request);
            onPaymentStarted();
        } catch (PaymentException e) {
            // handle all errors
            String errorMessage = "Unknown error";

            switch (e.getErrorCode()) {
                case CARD_EXPIRED:
                    errorMessage = "Card has expired";
                    break;
                case CARD_EXPIRY_INVALID:
                    errorMessage = "Invalid expiry date";
                    break;
                case CARD_PAN_INVALID:
                    errorMessage = "Invalid card number";
                    break;
                case CARD_PAN_INVALID_LUHN:
                    errorMessage = "Invalid card number";
                    break;
                case CARD_CV2_INVALID:
                    errorMessage = "Invalid CV2 number";
                    break;
                case TRANSACTION_INVALID_AMOUNT:
                    errorMessage = "Invalid transaction amount";
                    break;
                case TRANSACTION_INVALID_CURRENCY:
                    errorMessage = "Invalid transaction currency";
                    break;
                case NETWORK_NO_CONNECTION:
                    errorMessage = "No network connection";
                    break;
                case CREDENTIALS_INVALID:
                    errorMessage = "Please pass in token and installation id\"";
                    break;
            }
            showError(errorMessage);
        }
    }

    /**
     * Callback from SDK for payment succeeded
     * @param paymentSuccess
     */
    @Override
    public void paymentSucceeded(final com.paypoint.sdk.library.payment.PaymentSuccess paymentSuccess) {

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onPaymentEnded();

                // Show receipt activity passing across the paymentSuccess details
                Intent intent = new Intent(PaymentActivity.this, ReceiptActivity.class);
                intent.putExtra(ReceiptActivity.EXTRA_RECEIPT, paymentSuccess);
                startActivity(intent);
            }
        }, 2000);
    }

    /**
     * Callback from SDK for payment failed
     * @param paymentError
     */
    @Override
    public void paymentFailed(com.paypoint.sdk.library.payment.PaymentError paymentError) {
        onPaymentEnded();

        String reasonMessage = "";

        if (paymentError != null) {
            if (paymentError.getKind() == PaymentError.Kind.PAYPOINT) {
                reasonMessage = paymentError.getPayPointError().getReasonMessage();
            } else if (paymentError.getKind() == PaymentError.Kind.NETWORK) {
                reasonMessage = "Network error";
            }
        }
        showError("Payment Failed: \n" + reasonMessage);
    }

    private void showError(String message) {
        CustomMessageDialog messageDialog = CustomMessageDialog.newInstance("Error", message);
        messageDialog.show(getSupportFragmentManager(), "");
    }

    private void showMessage(String title, String message) {
        CustomMessageDialog messageDialog = CustomMessageDialog.newInstance(title, message);
        messageDialog.show(getSupportFragmentManager(), "");
    }

    private void onPaymentStarted() {
        waitDialog = CustomWaitDialog.newInstance("Processing...");
        waitDialog.show(getSupportFragmentManager(), "");
    }

    private void onPaymentEnded() {
        if (waitDialog != null) {
            waitDialog.dismiss();
            waitDialog = null;
        }
    }
}
