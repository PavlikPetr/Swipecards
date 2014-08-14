package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * Simple RelativeLayout with listener for keyboard close/open.
 */
public class KeyboardListenerLayout extends RelativeLayout {

    private KeyboardListener mKeyboardListener;
    private boolean mKeyboardOpened;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mKeyboardOpened = false;
        } else if (w == oldw) {
            if (h < oldh) {
                mKeyboardOpened = true;
            } else if (h > oldh) {
                mKeyboardOpened = false;
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setKeyboardListener(KeyboardListener keyboardListener) {
        mKeyboardListener = keyboardListener;
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

    {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mKeyboardListener != null) {
                    if (mKeyboardOpened) {
                        mKeyboardListener.keyboardOpened();
                    } else {
                        mKeyboardListener.keyboardClosed();
                    }
                }
            }
        });
    }

    public interface KeyboardListener {
        void keyboardOpened();
        void keyboardClosed();
    }
}
