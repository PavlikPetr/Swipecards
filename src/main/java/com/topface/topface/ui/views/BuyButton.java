package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Петр on 25.02.2016.
 * Parent for all buy buttons
 */
public abstract class BuyButton extends LinearLayout {
    public BuyButton(Context context) {
        super(context);
    }

    public BuyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BuyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void startWaiting();

    public abstract void stopWaiting();

    public boolean setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
            return true;
        }
        return false;
    }

    public void setText(TextView textView, String text) {
        if (setViewVisibility(textView, View.VISIBLE)) {
            textView.setText(text);
        }
    }
}
