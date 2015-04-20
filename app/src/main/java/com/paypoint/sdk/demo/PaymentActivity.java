package com.paypoint.sdk.demo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.paypoint.sdk.demo.merchant.MerchantTokenManager;
import com.paypoint.sdk.demo.utils.FontUtils;
import com.paypoint.sdk.demo.widget.CustomMessageDialog;
import com.paypoint.sdk.demo.widget.CustomWaitDialog;
import com.paypoint.sdk.library.exception.PaymentValidationException;
import com.paypoint.sdk.library.network.EndpointManager;
import com.paypoint.sdk.library.payment.PaymentError;
import com.paypoint.sdk.library.payment.PaymentManager;
import com.paypoint.sdk.library.payment.PaymentRequest;
import com.paypoint.sdk.library.payment.request.PaymentCard;
import com.paypoint.sdk.library.payment.request.Transaction;
import com.paypoint.sdk.library.security.PayPointCredentials;

import java.util.UUID;

import retrofit.RetrofitError;

public class PaymentActivity extends ActionBarActivity implements PaymentManager.MakePaymentCallback,
    MerchantTokenManager.GetTokenCallback {

//    private static final String URL_PAYPOINT = "http://10.0.3.2:5000/mobileapi";       // Genymotion
    private static final String URL_PAYPOINT = "http://192.168.3.138:5000/mobileapi";    // Pete's machine

//    private static final String URL_MERCHANT = "http://10.0.3.2:5001/merchant";       // Genymotion
    private static final String URL_MERCHANT = "http://192.168.3.138:5001/merchant";    // Pete's machine

    private EditText editCardNumber;
    private EditText editCardExpiry;
    private EditText editCardCvv;
    private Button buttonPay;
    private CustomWaitDialog waitDialog;
    private PaymentManager paymentManager;
    private MerchantTokenManager tokenManager;
    private PaymentRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        FontUtils.setFontForHierarchy(this, getWindow().getDecorView().findViewById(android.R.id.content));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.activity_payment_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editCardNumber = (EditText)findViewById(R.id.editCardNumber);
        editCardExpiry = (EditText)findViewById(R.id.editCardExpiry);
        editCardCvv = (EditText)findViewById(R.id.editCardCVV);
        buttonPay = (Button)findViewById(R.id.buttonPay);

        editCardNumber.addTextChangedListener(new CardNumberFormatter());

        // TEST DETAILS START
//        editCardNumber.setText("9900 0000 0000 5159");
//        editCardExpiry.setText("1115");
//        editCardCvv.setText("123");
        // TEXT DETAILS END

        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePayment();
            }
        });

        // instantiate the PaymentManager in the SDK
        paymentManager = new PaymentManager(this)
                .setUrl(URL_PAYPOINT);

        tokenManager = new MerchantTokenManager();
    }

    private void makePayment() {

        String cardNumber = editCardNumber.getText().toString();
        String cardExpiry = editCardExpiry.getText().toString();
        String cardCvv = editCardCvv.getText().toString();

        // build up the card payment
        PaymentCard card = new PaymentCard()
                .setPan(cardNumber)
                .setExpiryDate(cardExpiry)
                .setCv2(cardCvv);

        Transaction transaction = new Transaction()
                .setCurrency("GBP")
                .setAmount(100.00f)
                .setMerchantReference("PP_" + UUID.randomUUID().toString().substring(0, 8)); // Generate this in your app in whichever way suits

        // build the request
        request = new PaymentRequest()
                .setCallback(this)
                .setCard(card)
                .setTransaction(transaction);

        try {
            // validate payment details entered by user
            paymentManager.validatePaymentDetails(request);

            // start animation
            onPaymentStarted();

            // payment details valid - get Merchant token
            tokenManager.getMerchantToken(URL_MERCHANT, this);

        } catch (PaymentValidationException e) {
            showValidationError(e);
        }
    }

    private void showValidationError(PaymentValidationException e) {
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
            case INVALID_CREDENTIALS:
            case INVALID_CARD:
            case INVALID_REQUEST:
            case INVALID_TRANSACTION:
            case INVALID_URL:
                errorMessage = "Developer error - check arguments to makePayment";
                break;
        }
        showError(errorMessage);
    }

    /**
     * Callback when token received from merchant server
     * @param token
     */
    @Override
    public void getTokenSucceeded(String token) {

        // create the PayPoint credentials to use for the request
        PayPointCredentials credentials = new PayPointCredentials().setInstallationId("1212312")
                .setToken(token);

        paymentManager.setCredentials(credentials);

        try {
            // make the payment
            paymentManager.makePayment(request);
        } catch (PaymentValidationException e) {
            showValidationError(e);
        }
    }

    /**
     * Callback when error receiving token from merchant server
     * @param error
     */
    @Override
    public void getTokenFailed(RetrofitError error) {
        onPaymentEnded();
        showError("Failed to get merchant token");
    }

    /**
     * Callback from PayPoint SDK for payment succeeded
     * @param paymentSuccess
     */
    @Override
    public void paymentSucceeded(final com.paypoint.sdk.library.payment.PaymentSuccess paymentSuccess) {

        Handler handler = new Handler();

        // TODO postDelayed only used for localhost testing - remove once using a remote server
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onPaymentEnded();

                // show receipt activity passing across the paymentSuccess details
                Intent intent = new Intent(PaymentActivity.this, ReceiptActivity.class);
                intent.putExtra(ReceiptActivity.EXTRA_RECEIPT, paymentSuccess);
                finish();
                startActivity(intent);
            }
        }, 2000);
    }

    /**
     * Callback from PayPoint SDK for payment failed
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

    private void onPaymentStarted() {
        // show a wait dialog - this is just a PayPoint branded example!
        waitDialog = CustomWaitDialog.newInstance("Processing...");
        waitDialog.show(getSupportFragmentManager(), "");
    }

    private void onPaymentEnded() {
        if (waitDialog != null) {
            waitDialog.dismissAllowingStateLoss();
            waitDialog = null;
        }
    }
}
