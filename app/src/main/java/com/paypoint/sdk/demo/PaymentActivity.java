/*
 * Copyright (c) 2015. PayPoint
 */

package com.paypoint.sdk.demo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.paypoint.sdk.demo.merchant.MerchantTokenManager;
import com.paypoint.sdk.demo.utils.FontUtils;
import com.paypoint.sdk.demo.widget.CustomMessageDialog;
import com.paypoint.sdk.demo.widget.CustomWaitDialog;
import com.paypoint.sdk.demo.widget.ShakeableEditText;
import com.paypoint.sdk.library.exception.PaymentValidationException;
import com.paypoint.sdk.library.payment.PaymentError;
import com.paypoint.sdk.library.payment.PaymentManager;
import com.paypoint.sdk.library.payment.PaymentRequest;
import com.paypoint.sdk.library.payment.PaymentSuccess;
import com.paypoint.sdk.library.payment.request.PaymentCard;
import com.paypoint.sdk.library.payment.request.Transaction;
import com.paypoint.sdk.library.security.PayPointCredentials;

import java.util.UUID;

import retrofit.RetrofitError;

public class PaymentActivity extends ActionBarActivity implements PaymentManager.MakePaymentCallback,
    MerchantTokenManager.GetTokenCallback {

    /**
     * The following card numbers can be used for testing against the test payment server:
     * 9900 0000 0000 5159 – returns successful authorisation.
     * 9900 0000 0000 5282 – returns payment declined.
     * All other cards will return a server error.
     *
     */

    private ShakeableEditText editCardNumber;
    private ShakeableEditText editCardExpiry;
    private ShakeableEditText editCardCvv;
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

        editCardNumber = (ShakeableEditText)findViewById(R.id.editCardNumber);
        editCardExpiry = (ShakeableEditText)findViewById(R.id.editCardExpiry);
        editCardCvv = (ShakeableEditText)findViewById(R.id.editCardCVV);
        buttonPay = (Button)findViewById(R.id.buttonPay);

        initialiseInlineValidation();

        editCardNumber.addTextChangedListener(new CardNumberFormatter());

        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePayment();
            }
        });

        // instantiate the PaymentManager in the SDK
        paymentManager = PaymentManager.getInstance(this)
                .setUrl(getString(R.string.url_paypoint));

        tokenManager = new MerchantTokenManager();
    }

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

    /**
     * This is an example of how an app might do inline validation of payment fields
     * before the user commits to the payment
     */
    private void initialiseInlineValidation() {

        editCardNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        editCardNumber.clearError();
                        paymentManager.validateCardPan(editCardNumber.getText().toString());
                    } catch (PaymentValidationException e) {
                        switch (e.getErrorCode()) {
                            case CARD_PAN_INVALID:
                                editCardNumber.setError("Invalid card number");
                                break;
                            case CARD_PAN_INVALID_LUHN:
                                editCardNumber.setError("Invalid card number");
                                break;
                        }
                    }
                } else {
                    editCardNumber.showError();
                }
            }
        });

        editCardExpiry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        editCardExpiry.clearError();
                        paymentManager.validateCardExpiry(editCardExpiry.getText().toString());
                    } catch (PaymentValidationException e) {
                        switch (e.getErrorCode()) {
                            case CARD_EXPIRED:
                                editCardExpiry.setError("Card has expired");
                                break;
                            case CARD_EXPIRY_INVALID:
                                editCardExpiry.setError("Invalid expiry date");
                                break;
                        }
                    }
                } else {
                    editCardExpiry.showError();
                }
            }
        });

        editCardCvv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        editCardCvv.clearError();
                        paymentManager.validateCardCv2(editCardCvv.getText().toString());
                    } catch (PaymentValidationException e) {
                        switch (e.getErrorCode()) {
                            case CARD_CV2_INVALID:
                                editCardCvv.setError("Invalid CVV");
                                break;
                        }
                    }
                } else {
                    editCardCvv.showError();
                }
            }
        });
    }

    private void makePayment() {

        String cardNumber = editCardNumber.getText().toString();
        String cardExpiry = editCardExpiry.getText().toString();
        String cardCvv = editCardCvv.getText().toString();

        // MERCHANT TO IMPLEMENT - generate this according to your own requirements
        String merchantRef = "mer_" + UUID.randomUUID().toString().substring(0, 8);

        // build up the card payment
        PaymentCard card = new PaymentCard()
                .setPan(cardNumber)
                .setExpiryDate(cardExpiry)
                .setCv2(cardCvv);

        // currency and amount hardcoded in this instance for demo
        Transaction transaction = new Transaction()
                .setCurrency("GBP")
                .setAmount(100.00f)
                .setMerchantReference(merchantRef);

        // create the payment request
        request = new PaymentRequest()
                .setCard(card)
                .setTransaction(transaction);

        try {
            // locally validate payment details entered by user
            paymentManager.validatePaymentDetails(request);

            // start the wait animation - customise this according to your own branding
            onPaymentStarted();

            // MERCHANT TO IMPLEMENT - payment details valid, now get merchant token
            tokenManager.getMerchantToken(getString(R.string.url_merchant), this);

        } catch (PaymentValidationException e) {
            showValidationError(e);
        }
    }

    private void showValidationError(PaymentValidationException e) {

        onPaymentEnded();

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
        PayPointCredentials credentials = new PayPointCredentials()
                .setInstallationId(getString(R.string.installation_id))
                .setToken(token);

        paymentManager.setCredentials(credentials);

        try {
            // now make the payment
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
    public void paymentSucceeded(final PaymentSuccess paymentSuccess) {
        onPaymentEnded();

        // show receipt activity passing across the paymentSuccess details
        Intent intent = new Intent(PaymentActivity.this, ReceiptActivity.class);
        intent.putExtra(ReceiptActivity.EXTRA_RECEIPT, paymentSuccess);
        finish();
        startActivity(intent);

    }

    /**
     * Callback from PayPoint SDK for payment failed
     * @param paymentError
     */
    @Override
    public void paymentFailed(PaymentError paymentError) {
        onPaymentEnded();

        String reasonMessage = "";

        if (paymentError != null) {
            if (paymentError.getKind() == PaymentError.Kind.PAYPOINT) {
                reasonMessage = paymentError.getPayPointError().getReasonMessage();

                // PayPointError also provides an error enum
                PaymentError.ReasonCode reasonCode = paymentError.getPayPointError().getReasonCode();
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
