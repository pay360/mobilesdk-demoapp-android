package com.paypoint.sdk.demo.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jamie on 02/10/2014.
 */
public class FontUtils {

    private static Typeface FONT_REGULAR;

    private static boolean fontsNotLoaded = true;

    private static void loadCustomFont(Context context) {
        FONT_REGULAR = readFont(context, FONT_REGULAR, "FOUCONRG.otf");
    }

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

    /**
     * Sets all text to the custom font, initialising the typeface if not already loaded
     * @param container
     */
    public static void setFontForHierarchy(Context context, View container) {
        setFontForHierarchy(context, (ViewGroup) container);
    }

    /**
     * Sets all text to the custom font, initialising the typeface if not already loaded
     * @param container
     */
    public static void setFontForHierarchy(Context context, ViewGroup container) {

        if (fontsNotLoaded) {
            loadCustomFont(context);
            fontsNotLoaded = false;
        }

        if (container == null) {
            Log.e("FontUtils", "Container null");
            return;
        }
        if (FONT_REGULAR == null) {
            Log.e("FontUtils", "One or more custom fonts null");
            return;
        }

        final int count = container.getChildCount();

        // Loop through all of the children.
        for (int i = 0; i < count; ++i)
        {
            final View child = container.getChildAt(i);
            if (child instanceof TextView) {
                // Set the font if it is a TextView.
                TextView view = (TextView) child;

                view.setTypeface(FONT_REGULAR, Typeface.NORMAL);
            } else if (child instanceof ViewGroup) {
                // Recursively attempt another ViewGroup.
                setFontForHierarchy(context, (ViewGroup) child);
            }
        }
    }

}
