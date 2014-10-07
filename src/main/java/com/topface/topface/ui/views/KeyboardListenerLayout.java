package com.topface.topface.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * Simple RelativeLayout with listener for keyboard close/open.
 */
public class KeyboardListenerLayout extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    private KeyboardListener mKeyboardListener;
    private boolean mKeyboardOpened;
    private boolean mWasToggled;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mKeyboardOpened = false;
        } else if (w == oldw) {
            if ((float) oldh / h > 1.25) {
                mKeyboardOpened = true;
                mWasToggled = true;
            } else if ((float) oldh / h < 0.75) {
                mKeyboardOpened = false;
                mWasToggled = true;
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setKeyboardListener(KeyboardListener keyboardListener) {
        mKeyboardListener = keyboardListener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        if (mKeyboardListener != null && mWasToggled) {
            if (mKeyboardOpened) {
                mKeyboardListener.keyboardOpened();
            } else {
                mKeyboardListener.keyboardClosed();
            }
            mWasToggled = false;
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void removeGlobalLayoutListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeGlobalLayoutListener();
    }

    @SuppressWarnings("unused")
    public KeyboardListenerLayout(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public KeyboardListenerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public KeyboardListenerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public interface KeyboardListener {
        void keyboardOpened();
        void keyboardClosed();
    }

    public boolean isKeyboardOpened() {
        return mKeyboardOpened;
    }
}
