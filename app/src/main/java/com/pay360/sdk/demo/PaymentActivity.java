/*
 * Copyright (c) 2016 Capita plc
 */

package com.pay360.sdk.demo;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pay360.sdk.demo.merchant.MerchantTokenManager;
import com.pay360.sdk.demo.widget.CustomMessageDialog;
import com.pay360.sdk.demo.widget.CustomWaitDialog;
import com.pay360.sdk.demo.widget.ShakeableEditText;
import com.pay360.sdk.library.exception.InvalidCredentialsException;
import com.pay360.sdk.library.exception.PaymentValidationException;
import com.pay360.sdk.library.exception.TransactionInProgressException;
import com.pay360.sdk.library.exception.TransactionSuspendedFor3DSException;
import com.pay360.sdk.library.network.EndpointManager;
import com.pay360.sdk.library.payment.BillingAddress;
import com.pay360.sdk.library.payment.PaymentError;
import com.pay360.sdk.library.payment.PaymentManager;
import com.pay360.sdk.library.payment.PaymentRequest;
import com.pay360.sdk.library.payment.PaymentSuccess;
import com.pay360.sdk.library.payment.PaymentCard;
import com.pay360.sdk.library.payment.Transaction;
import com.pay360.sdk.library.security.Credentials;


import java.util.UUID;

import retrofit.RetrofitError;

public class PaymentActivity extends ActionBarActivity implements PaymentManager.MakePaymentCallback,
    MerchantTokenManager.GetTokenCallback {

    // Copy your installation id here
    private static final String INSTALLATION_ID = "5300311";

    private ShakeableEditText editCardNumber;
    private ShakeableEditText editCardExpiry;
    private ShakeableEditText editCardCvv;
    private Button buttonPay;
    private EditText editAmount;

    private PaymentManager paymentManager;
    private MerchantTokenManager tokenManager;
    private PaymentRequest request;
    private String operationId;
    private String merchantUrl;
    private String installationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

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
        editAmount = (EditText)findViewById(R.id.editAmount);
        buttonPay = (Button)findViewById(R.id.buttonPay);

        initialiseInlineValidation();

        editCardNumber.addTextChangedListener(new CardNumberFormatter());

        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePayment();
            }
        });

        // read url from build first, fallback to local config
        String serverUrl = getString(R.string.build_url_server);
        
        if (TextUtils.isEmpty(serverUrl)) {
            serverUrl = EndpointManager.getEndpointUrl(EndpointManager.Environment.MITE);
        }

        // instantiate the PaymentManager in the SDK pointing to MITE
        paymentManager = PaymentManager.getInstance(this)
                .setUrl(serverUrl);

        tokenManager = new MerchantTokenManager();

        // read url from build first, fallback to local config
        merchantUrl = getString(R.string.build_url_merchant);

        if (TextUtils.isEmpty(merchantUrl)) {
            merchantUrl = getString(R.string.url_merchant);
        }

        installationId = getString(R.string.build_installation_id);

        if (TextUtils.isEmpty(installationId)) {
            installationId = INSTALLATION_ID;
        }
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
                                editCardNumber.setError(getString(R.string.error_invalid_pan));
                                break;
                            case CARD_PAN_INVALID_LUHN:
                                editCardNumber.setError(getString(R.string.error_invalid_luhn));
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
                                editCardExpiry.setError(getString(R.string.error_expired));
                                break;
                            case CARD_EXPIRY_INVALID:
                                editCardExpiry.setError(getString(R.string.error_expiry_invalid));
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
                                editCardCvv.setError(getString(R.string.error_invalid_cvv));
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

        float amount = 0;

        if (!TextUtils.isEmpty(editAmount.getText().toString())) {
            amount = Float.parseFloat(editAmount.getText().toString());
        }

        // build up the card payment
        PaymentCard card = new PaymentCard()
                .setPan(cardNumber)
                .setExpiryDate(cardExpiry)
                .setCv2(cardCvv)
                .setCardHolderName("Mr A Smith");

        // currency and amount hardcoded in this instance for demo
        Transaction transaction = new Transaction()
                .setCurrency("GBP")
                .setAmount(amount)
                .setMerchantReference(merchantRef);

        BillingAddress address = new BillingAddress()
                .setLine1("House Name")
                .setLine2("Street")
                .setCity("Bath")
                .setRegion("Somerset")
                .setPostcode("BA1 5BG");

        // create the payment request
        request = new PaymentRequest()
                .setCard(card)
                .setTransaction(transaction)
                .setAddress(address);

        try {
            // locally validate payment details entered by user
            paymentManager.validatePaymentDetails(request);

            // start the wait animation - customise this according to your own branding
            onPaymentStarted();

            // MERCHANT TO IMPLEMENT - payment details valid, now get merchant token
            tokenManager.getMerchantToken(merchantUrl, installationId, this);

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
                errorMessage = getString(R.string.error_expired);
                break;
            case CARD_EXPIRY_INVALID:
                errorMessage = getString(R.string.error_expiry_invalid);
                break;
            case CARD_PAN_INVALID:
                errorMessage = getString(R.string.error_invalid_pan);
                break;
            case CARD_PAN_INVALID_LUHN:
                errorMessage = getString(R.string.error_invalid_luhn);
                break;
            case CARD_CV2_INVALID:
                errorMessage = getString(R.string.error_invalid_cvv);
                break;
            case NETWORK_NO_CONNECTION:
                errorMessage = getString(R.string.error_no_network);
                break;
            case TRANSACTION_INVALID_AMOUNT:
            case TRANSACTION_INVALID_CURRENCY:
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

        // create the credentials to use for the request
        Credentials credentials = new Credentials()
                .setInstallationId(installationId)
                .setToken(token);

        paymentManager.setCredentials(credentials);

        try {
            // now make the payment - store the operationId returned, this can be used to check the
            // state of a transaction
            operationId = paymentManager.makePayment(request);
        } catch (PaymentValidationException e) {
            showValidationError(e);
        } catch (TransactionInProgressException e) {
            onPaymentEnded();

            showError("Payment currently in progress");
        } catch (InvalidCredentialsException e) {
            onPaymentEnded();

            showError("Developer error - check arguments to makePayment");
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
     * Callback from SDK for payment succeeded
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
     * Callback from SDK for payment failed
     * @param paymentError
     */
    @Override
    public void paymentFailed(PaymentError paymentError) {
        onPaymentEnded();

        String errorMessage = "Unexpected error.";
        boolean retryPayment = false;

        if (paymentError != null) {
            // paymentError.getReasonMessage() should be used for debugging only

            // PaymentError also provides an error enum
            PaymentError.ReasonCode reasonCode = paymentError.getReasonCode();

            // if isSafeToRetryPayment() returns true then payment has not been taken
            retryPayment = reasonCode.isSafeToRetryPayment();

            switch (reasonCode) {

                case NETWORK_ERROR_DURING_PROCESSING:
                    errorMessage = "Network error during transaction.";
                    break;

                case NETWORK_NO_CONNECTION:
                    errorMessage = "No network connection, please retry.";
                    break;

                case TRANSACTION_TIMED_OUT:
                    errorMessage = "Transaction timed out waiting for a response.";
                    break;

                case TRANSACTION_CANCELLED_BY_USER:
                    errorMessage = "Transaction cancelled by the user.";
                    break;

                case UNEXPECTED:
                    errorMessage = "Something went wrong, we don't know what.";
                    break;

                case INVALID:
                    errorMessage = "Invalid request sent to the server.";
                    break;

                case TRANSACTION_DECLINED:
                    errorMessage = "The transaction was declined.";
                    break;

                case SERVER_ERROR:
                    errorMessage = "An internal server error occurred.";
                    break;

                case TRANSACTION_NOT_FOUND:
                    errorMessage = "The transaction failed.";
                    break;

                case AUTHENTICATION_FAILED:
                    errorMessage = "The merchant token is incorrect.";
                    break;

                case CLIENT_TOKEN_EXPIRED:
                    errorMessage = "The merchant token has expired.";
                    break;

                case UNAUTHORISED_REQUEST:
                    errorMessage = "The merchant token does not grant you access to making a payment.";
                    break;

            }
        }

        if (retryPayment) {
            errorMessage += "\nNo money has been taken from your account.";
        }

        showError("Payment Failed: \n" + errorMessage, !retryPayment);
    }

    /**
     * Callback if getStatus button clicked
     */
    public void onGetPaymentStatus() {
        try {
            paymentManager.getTransactionStatus(operationId);
            onPaymentStarted();
        } catch (InvalidCredentialsException e) {
            showError("Developer error - check arguments to makePayment");
        } catch (TransactionSuspendedFor3DSException e) {
            showError("Payment suspending for 3D Secure");
        } catch (TransactionInProgressException e) {
            showError("Transaction is in progress, please wait for callback");
        }
    }

    private void showError(String message) {
        showError(message, false);
    }

    private void showError(String message, boolean checkStatus) {
        CustomMessageDialog messageDialog = CustomMessageDialog.newInstance("Error", message, checkStatus);
        messageDialog.show(getFragmentManager(), "");
    }

    private void onPaymentStarted() {
        // show a wait dialog - this is just a Pay360 branded example!
        CustomWaitDialog waitDialog = CustomWaitDialog.newInstance("Processing...");
        waitDialog.show(getFragmentManager(), "WAIT_DIALOG");
    }

    private void onPaymentEnded() {

        Fragment waitDialog = getFragmentManager().findFragmentByTag("WAIT_DIALOG");

        if (waitDialog != null) {
            getFragmentManager().beginTransaction().remove(waitDialog).commitAllowingStateLoss();
        }
    }
}
