package com.paypoint.sdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.paypoint.sdk.demo.utils.FontUtils;

/**
 * Who:  Pete
 * When: 17/04/2015
 * What:
 */
public class SplashActivity extends ActionBarActivity {

    private Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FontUtils.setFontForHierarchy(this, getWindow().getDecorView().findViewById(android.R.id.content));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.activity_splash_title);

        TextView labelPayPoint = (TextView)findViewById(R.id.labelPayPoint);

        FontUtils.setFoundryBold(this, labelPayPoint);

        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, PaymentActivity.class));
            }
        });

        final ViewGroup containerPayPoint = (ViewGroup) findViewById(R.id.containerPayPoint);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                containerPayPoint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing to do
            }
        });

        containerPayPoint.startAnimation(fadeInAnimation);
    }
}
