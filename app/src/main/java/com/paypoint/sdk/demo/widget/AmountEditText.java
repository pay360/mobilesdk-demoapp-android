/*
 * Copyright (c) 2015. PayPoint
 */

package com.paypoint.sdk.demo.widget;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmountEditText extends EditText {

    public AmountEditText(Context context) {
        super(context);

        initialise();
    }

    public AmountEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialise();
    }

    public AmountEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialise();
    }

    private void initialise() {
        setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
    }

    private class DecimalDigitsInputFilter implements InputFilter {
        Pattern pattern;
        public DecimalDigitsInputFilter(int digitsAfterZero) {
            pattern = Pattern.compile("[0-9]+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher= pattern.matcher(dest);
            if(!matcher.matches()) {
                return "";
            }
            return null;
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        // ensure view loses focus when keyboard dismissed
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
