/*
 * Copyright (c) 2015. PayPoint
 */

package com.paypoint.sdk.demo.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    public static CustomMessageDialog newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);

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

        Button okButton = (Button) d.findViewById(R.id.buttonOK);

        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                d.dismiss();
            }

        });

        return d;
    }
}
