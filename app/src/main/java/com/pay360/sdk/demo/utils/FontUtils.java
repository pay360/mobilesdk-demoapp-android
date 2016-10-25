/*
 * Copyright (c) 2016. Pay360
 */

package com.pay360.sdk.demo.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Utilities for setting view fonts
 */
public class FontUtils {

    private static Typeface FONT_AWESOME;

    private static Typeface readFont(Context context, Typeface font, String fontPath) {
        if (font == null) {
            try {
                font = Typeface.createFromAsset(context.getAssets(), fontPath);
            } catch (Exception e) {
                Log.e("BButton", "Could not get typeface", e);
                font = Typeface.DEFAULT;
            }
        }
        return font;
    }

    public static void setFontAwesome(Context context, TextView textView) {

        FONT_AWESOME = readFont(context, FONT_AWESOME, "FontAwesome.otf");

        if (FONT_AWESOME != null) {
            textView.setTypeface(FONT_AWESOME, Typeface.NORMAL);
        }
    }
}
