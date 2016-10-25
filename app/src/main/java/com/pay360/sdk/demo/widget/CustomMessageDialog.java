/*
 * Copyright (c) 2016. Pay360
 */

package com.pay360.sdk.demo.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pay360.sdk.demo.PaymentActivity;

public class CustomMessageDialog extends DialogFragment {

    private static final String ARG_TITLE = "CustomMessageDialog.ARG_TITLE";
    private static final String ARG_MESSAGE = "CustomMessageDialog.ARG_MESSAGE";
    private static final String ARG_CHECK_STATUS = "CustomMessageDialog.ARG_CHECK_STATUS";

    public static CustomMessageDialog newInstance(String title, String message, boolean retry) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_CHECK_STATUS, retry);

        CustomMessageDialog fragment = new CustomMessageDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog d = new Dialog(getActivity(), com.pay360.sdk.demo.R.style.dialog);

        d.setContentView(com.pay360.sdk.demo.R.layout.custom_message_dialog);

        TextView titleView = (TextView)d.findViewById(com.pay360.sdk.demo.R.id.textTitle);

        titleView.setText(getArguments().getString(ARG_TITLE));

        TextView messageView = (TextView) d.findViewById(com.pay360.sdk.demo.R.id.textMessage);

        messageView.setText(getArguments().getString(ARG_MESSAGE));

        Button closeButton = (Button) d.findViewById(com.pay360.sdk.demo.R.id.buttonClose);

        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                d.dismiss();
            }

        });

        Button checkStatusButton = (Button) d.findViewById(com.pay360.sdk.demo.R.id.buttonCheckStatus);

        if (getArguments().getBoolean(ARG_CHECK_STATUS, false)) {
            checkStatusButton.setVisibility(View.VISIBLE);
        }

        checkStatusButton.setOnClickListener(new View.OnClickListener() {
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
