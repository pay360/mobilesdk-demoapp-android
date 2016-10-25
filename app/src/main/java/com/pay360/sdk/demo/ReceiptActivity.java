/*
 * Copyright (c) 2016. Pay360
 */

package com.pay360.sdk.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.pay360.sdk.demo.utils.FontUtils;
import com.paypoint.sdk.library.payment.PaymentSuccess;

public class ReceiptActivity extends ActionBarActivity {

    public static final String EXTRA_RECEIPT = "com.pay360.sdk.demo.EXTRA_RECEIPT";
    private static final int ANIMATION_DELAY = 1000;

    private TextView textTick;
    private TextView textCardNumber;
    private TextView textMerchantRef;
    private TextView textTransactionId;
    private TextView textAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.activity_receipt_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        textTick = (TextView)findViewById(R.id.labelTick);
        textCardNumber = (TextView)findViewById(R.id.textCardNumberMasked);
        textMerchantRef = (TextView)findViewById(R.id.textMechantRef);
        textTransactionId = (TextView)findViewById(R.id.textTransactionId);
        textAmount = (TextView)findViewById(R.id.textAmount);

        FontUtils.setFontAwesome(this, textTick);

        PaymentSuccess paymentSuccess = (PaymentSuccess)getIntent().getSerializableExtra(EXTRA_RECEIPT);

        if (paymentSuccess != null) {
            displayReceipt(paymentSuccess);
        }
    }

    /**
     * Show the receipt details
     * @param paymentSuccess
     */
    private void displayReceipt(PaymentSuccess paymentSuccess) {
        startAnimation();

        // Populate the receipt
        textCardNumber.setText(paymentSuccess.getMaskedPan());
        textMerchantRef.setText(paymentSuccess.getMerchantReference());
        textTransactionId.setText(paymentSuccess.getTransactionId());
        textAmount.setText(getString(R.string.receipt_amount_formatted, paymentSuccess.getAmount()));
    }

    /**
     * Start animating the tick using the Rebound library
     */
    private void startAnimation() {
        SpringSystem springSystem = SpringSystem.create();

        // Add a spring to the system.
        final Spring spring = springSystem.createSpring();

        // Add a listener to observe the motion of the spring.
        spring.addListener(new SimpleSpringListener() {

            @Override
            public void onSpringUpdate(Spring spring) {
                double value = spring.getCurrentValue();

                // Map the spring to the selected photo scale as it moves into and out of the grid.
                float scale = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 0.5, 0, 1);
                textTick.setScaleY(scale);
                textTick.setScaleX(scale);
            }
        });

        // Set the spring in motion; moving from 0 to 1
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textTick.setVisibility(View.VISIBLE);
                spring.setEndValue(1);
            }
        }, ANIMATION_DELAY);
    }
}
