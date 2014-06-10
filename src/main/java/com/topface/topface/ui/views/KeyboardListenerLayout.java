package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Simple RelativeLayout with listener for keyboard close/open.
 */
public class KeyboardListenerLayout extends RelativeLayout {

    private KeyboardListener mKeyboardListener;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mKeyboardListener != null) {
            if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mKeyboardListener.keyboardClosed();
            } else if (w == oldw) {
                if (h < oldh) {
                    mKeyboardListener.keyboardOpened();
                } else if (h > oldh) {
                    mKeyboardListener.keyboardClosed();
                }
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setKeyboardListener(KeyboardListener keyboardListener) {
        mKeyboardListener = keyboardListener;
    }
    public KeyboardListenerLayout(Context context) {
        super(context);
    }

    public KeyboardListenerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardListenerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface KeyboardListener {
        void keyboardOpened();
        void keyboardClosed();
    }
}
