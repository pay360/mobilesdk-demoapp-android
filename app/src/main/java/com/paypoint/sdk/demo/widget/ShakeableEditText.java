/*
 * Copyright (c) 2016. Pay360
 */

package com.paypoint.sdk.demo.widget;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.paypoint.sdk.demo.R;

public class ShakeableEditText extends EditText {

    private String error;

    public ShakeableEditText(Context context) {
        super(context);
    }

    public ShakeableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShakeableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void showError() {
        super.setError(error);
    }

    public void clearError() {
        this.error = null;
        super.setError(null);
    }

    @Override
    public void setError(CharSequence error) {

        if (error != null) {
            this.error = error.toString();
        } else {
            this.error = null;
        }

        super.setError(error);

        // shake if not clearing error
        if (error != null) {
            shake();
        }
    }

    /**
     * Shake the edit text
     */
    private void shake() {
        int delta = getResources().getDimensionPixelOffset(R.dimen.shake_spacing);

        PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, 0),
                Keyframe.ofFloat(.10f, -delta),
                Keyframe.ofFloat(.26f, delta),
                Keyframe.ofFloat(.42f, -delta),
                Keyframe.ofFloat(.58f, delta),
                Keyframe.ofFloat(.74f, -delta),
                Keyframe.ofFloat(.90f, delta),
                Keyframe.ofFloat(1f, 0f)
        );

        ObjectAnimator.ofPropertyValuesHolder(this, pvhTranslateX).
                setDuration(500).start();
    }
}
