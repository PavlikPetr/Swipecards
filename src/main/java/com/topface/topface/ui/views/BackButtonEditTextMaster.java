package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

/**
 * Simple EditText. The only difference is that you can set listener on back button when you press it to close keyboard.
 */
@SuppressWarnings("UnusedDeclaration")
public class BackButtonEditTextMaster extends EditText {

    private OnKeyBoardExitedListener mOnKeyBoardExitedListener;

    public BackButtonEditTextMaster(Context context) {
        super(context);
    }

    public BackButtonEditTextMaster(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackButtonEditTextMaster(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NotNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mOnKeyBoardExitedListener.onKeyboardExited();
        }
        return false;
    }

    public void setOnKeyBoardExitedListener(OnKeyBoardExitedListener listener) {
        mOnKeyBoardExitedListener = listener;
    }

    public static interface OnKeyBoardExitedListener {
        void onKeyboardExited();
    }
}
