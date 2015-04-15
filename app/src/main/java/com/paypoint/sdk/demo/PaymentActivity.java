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
import com.paypoint.sdk.library.exception.NoNetworkException;
import com.paypoint.sdk.library.exception.TransactionInvalidAmountException;
import com.paypoint.sdk.library.exception.TransactionInvalidCurrencyException;
import com.paypoint.sdk.library.payment.PaymentManager;
import com.paypoint.sdk.library.payment.PaymentRequest;
import com.paypoint.sdk.library.payment.request.PaymentCard;
import com.paypoint.sdk.library.payment.request.Transaction;
import com.paypoint.sdk.library.security.PayPointCredentials;

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

        PaymentManager paymentManager = new PaymentManager(this);

        PaymentCard card = new PaymentCard().setPan(cardNumber).setExpiryDate(cardExpiry).setCv2(cardCvv);

        Transaction transaction = new Transaction().setCurrency("GBP").setAmount(100);

        PayPointCredentials credentials = new PayPointCredentials().setToken("VALID_TOKEN");

        PaymentRequest request = new PaymentRequest()
                .setCallback(this)
                .setCard(card)
                .setTransaction(transaction)
                .setUrl("http://10.0.3.2:5000/mobileapi/transactions")
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
        }
    }

    @Override
    public void paymentSucceeded(com.paypoint.sdk.library.payment.PaymentSuccess paymentSuccess) {
        onPaymentEnded();

        // TODO show receipt
    }

    @Override
    public void paymentFailed(com.paypoint.sdk.library.payment.PaymentError paymentError) {
        onPaymentEnded();

        // TODO show error
    }

    private void showError(String message) {
        CustomMessageDialog messageDialog = CustomMessageDialog.newInstance("Error", message);
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
