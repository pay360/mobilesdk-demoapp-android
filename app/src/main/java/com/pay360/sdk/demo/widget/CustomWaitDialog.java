/*
 * Copyright (c) 2016. Pay360
 */

package com.pay360.sdk.demo.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomWaitDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "com.pay360.sdk.demo.widget.ARG_MESSAGE";

    public static CustomWaitDialog newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);

        CustomWaitDialog fragment = new CustomWaitDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(getActivity(), com.pay360.sdk.demo.R.style.dialog);

        d.setCanceledOnTouchOutside(false);
        setCancelable(false);

        d.setContentView(com.pay360.sdk.demo.R.layout.custom_progress_dialog);

        TextView titleView = (TextView)d.findViewById(com.pay360.sdk.demo.R.id.textMessage);

        Animation waitAnimation = AnimationUtils.loadAnimation(this.getActivity(), com.pay360.sdk.demo.R.anim.pulse);

        ImageView imageLogo = (ImageView)d.findViewById(com.pay360.sdk.demo.R.id.imageLogo);

        imageLogo.startAnimation(waitAnimation);

        titleView.setText(getArguments().getString(ARG_MESSAGE));

        return d;
    }
}