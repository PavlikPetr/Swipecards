package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

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
}
