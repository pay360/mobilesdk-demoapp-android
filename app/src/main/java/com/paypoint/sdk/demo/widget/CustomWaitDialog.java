/*
 * Copyright (c) 2016. Pay360
 */

package com.paypoint.sdk.demo.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.paypoint.sdk.demo.R;
import com.paypoint.sdk.demo.utils.FontUtils;

public class CustomWaitDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "com.paypoint.sdk.demo.widget.ARG_MESSAGE";

    public static CustomWaitDialog newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);

        CustomWaitDialog fragment = new CustomWaitDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(getActivity(), R.style.dialog);

        d.setCanceledOnTouchOutside(false);
        setCancelable(false);

        d.setContentView(R.layout.custom_progress_dialog);

        TextView titleView = (TextView)d.findViewById(R.id.textMessage);

        Animation waitAnimation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.pulse);

        ImageView imageLogo = (ImageView)d.findViewById(R.id.imageLogo);

        imageLogo.startAnimation(waitAnimation);

        titleView.setText(getArguments().getString(ARG_MESSAGE));

        return d;
    }
}