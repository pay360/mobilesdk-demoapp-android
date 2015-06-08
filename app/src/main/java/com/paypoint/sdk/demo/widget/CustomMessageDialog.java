/*
 * Copyright (c) 2015. PayPoint
 */

package com.paypoint.sdk.demo.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paypoint.sdk.demo.PaymentActivity;
import com.paypoint.sdk.demo.R;
import com.paypoint.sdk.demo.utils.FontUtils;

/**
 * Who:  Pete
 * When: 15/04/2015
 * What: PayPoint styled message dialog
 */
public class CustomMessageDialog extends DialogFragment {

    private static final String ARG_TITLE = "com.paypoint.sdk.demo.widget.CustomMessageDialog.ARG_TITLE";
    private static final String ARG_MESSAGE = "com.paypoint.sdk.demo.widget.CustomMessageDialog.ARG_MESSAGE";
    private static final String ARG_RETRY = "com.paypoint.sdk.demo.widget.CustomMessageDialog.ARG_RETRY";

    public static CustomMessageDialog newInstance(String title, String message, boolean retry) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_RETRY, retry);

        CustomMessageDialog fragment = new CustomMessageDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog d = new Dialog(getActivity(), R.style.dialog);

        d.setContentView(R.layout.custom_message_dialog);

        FontUtils.setFontForHierarchy(this.getActivity(), d.findViewById(R.id.viewRoot));

        TextView titleView = (TextView)d.findViewById(R.id.textTitle);

        titleView.setText(getArguments().getString(ARG_TITLE));

        TextView messageView = (TextView) d.findViewById(R.id.textMessage);

        messageView.setText(getArguments().getString(ARG_MESSAGE));

        Button closeButton = (Button) d.findViewById(R.id.buttonClose);

        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                d.dismiss();
            }

        });

        Button retryButton = (Button) d.findViewById(R.id.buttonRetry);

        if (getArguments().getBoolean(ARG_RETRY, false)) {
            retryButton.setVisibility(View.VISIBLE);
        }

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                // request payment status
                ((PaymentActivity)getActivity()).onGetPaymentStatus();
            }
        });

        return d;
    }
}
