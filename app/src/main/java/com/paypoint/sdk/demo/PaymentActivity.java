package com.paypoint.sdk.demo;

import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.paypoint.sdk.demo.widget.CustomMessageDialog;
import com.paypoint.sdk.demo.widget.CustomWaitDialog;
import com.paypoint.sdk.library.exception.CardExpiredException;
import com.paypoint.sdk.library.exception.CardInvalidCv2Exception;
import com.paypoint.sdk.library.exception.CardInvalidExpiryException;
import com.paypoint.sdk.library.exception.CardInvalidLuhnException;
import com.paypoint.sdk.library.exception.CardInvalidPanException;
import com.paypoint.sdk.library.exception.CredentialMissingException;
import com.paypoint.sdk.library.exception.NoNetworkException;
import com.paypoint.sdk.library.exception.TransactionInvalidAmountException;
import com.paypoint.sdk.library.exception.TransactionInvalidCurrencyException;
import com.paypoint.sdk.library.payment.PaymentError;
import com.paypoint.sdk.library.payment.PaymentManager;
import com.paypoint.sdk.library.payment.PaymentRequest;
import com.paypoint.sdk.library.payment.request.BillingAddress;
import com.paypoint.sdk.library.payment.request.PaymentCard;
import com.paypoint.sdk.library.payment.request.Transaction;
import com.paypoint.sdk.library.security.PayPointCredentials;

import java.util.UUID;

public class PaymentActivity extends ActionBarActivity implements PaymentManager.MakePaymentCallback {

    private EditText editCardNumber;
    private EditText editCardExpiry;
    private EditText editCardCvv;
    private Button buttonPay;
    private CustomWaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editCardNumber = (EditText)findViewById(R.id.editCardNumber);
        editCardExpiry = (EditText)findViewById(R.id.editCardExpiry);
        editCardCvv = (EditText)findViewById(R.id.editCardCVV);
        buttonPay = (Button)findViewById(R.id.buttonPay);

        editCardNumber.addTextChangedListener(new CardNumberFormatter());

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
                .setAmount(100)
                .setMerchantReference(UUID.randomUUID().toString()); // Generate this in your app in whichever way suits

        // Use test credentials
        PayPointCredentials credentials = new PayPointCredentials().setInstallationId("1212312")
                .setToken("VALID_TOKEN");

        // Make the payment handling any errors
        PaymentRequest request = new PaymentRequest()
                .setCallback(this)
                .setCard(card)
                .setAddress(address)
                .setTransaction(transaction)
                .setUrl("http://192.168.3.138:5000/mobileapi")
                .setCredentials(credentials);

        try {
            paymentManager.makePayment(request);
            onPaymentStarted();
        } catch (NoNetworkException e) {
            showError("No Network");
        } catch (CardInvalidExpiryException e) {
            showError("Invalid Expiry Date");
        } catch (CardExpiredException e) {
            showError("Card Expired");
        } catch (CardInvalidCv2Exception e) {
            showError("Invalid CVV");
        } catch (CardInvalidPanException e) {
            showError("Invalid Card Number");
        } catch (CardInvalidLuhnException e) {
            showError("Invalid Card Number");
        } catch (TransactionInvalidAmountException e) {
            showError("Invalid Amount");
        } catch (TransactionInvalidCurrencyException e) {
            showError("Invalid Currency");
        } catch (CredentialMissingException e) {
            showError("Please pass in token and installation id");
        }
    }

    /**
     * Callback from SDK for payment succeeded
     * @param paymentSuccess
     */
    @Override
    public void paymentSucceeded(com.paypoint.sdk.library.payment.PaymentSuccess paymentSuccess) {
        onPaymentEnded();

        showMessage("Success", "Your payment was successful!");
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
