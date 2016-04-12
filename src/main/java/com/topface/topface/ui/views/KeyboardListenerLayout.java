package com.topface.topface.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

/**
 * Simple RelativeLayout with listener for keyboard close/open.
 */
public class KeyboardListenerLayout extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final int DEFAULT_LAYOUT_SIZE_IN_PERCENT = 70;
    private static final int KEYBOARD_SIZE_IN_PERCENT = 30;

    private KeyboardListener mKeyboardListener;
    private boolean mKeyboardOpened;
    private boolean mWasToggled;
    private int mLayoutSizeInPercent = DEFAULT_LAYOUT_SIZE_IN_PERCENT;
    private Context mContext;
    private int mCurrentMax = -1;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mCurrentMax == -1) {
            mCurrentMax = getHeight();
        }
        if (h >= mCurrentMax) {
            Debug.log("ChatKeyboardListener -> skip height " + h);
            return;
        }
        if (w == oldw || oldw == 0) {
            mWasToggled = true;
            mKeyboardOpened = isKeyboardOpenedOnStart(h);
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
            mKeyboardListener.keyboardChangeState();
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
        initVariables(context);
    }

    @SuppressWarnings("unused")
    public KeyboardListenerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVariables(context);
        getAttributes(attrs);
    }

    @SuppressWarnings("unused")
    public KeyboardListenerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVariables(context);
        getAttributes(attrs);
    }

    private void getAttributes(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = null;
        try {
            a = getContext().obtainStyledAttributes(attrs, R.styleable.SoftKeyBoardListenerView);
            setLayoutSizeInPercent(a.getInt(R.styleable.SoftKeyBoardListenerView_estimatedLayoutHeight, DEFAULT_LAYOUT_SIZE_IN_PERCENT));
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    public void setLayoutSizeInPercent(int size) {
        mLayoutSizeInPercent = size > 100 ? 100 : size;
        mLayoutSizeInPercent = size <= 0 ? DEFAULT_LAYOUT_SIZE_IN_PERCENT : mLayoutSizeInPercent;
    }

    public interface KeyboardListener {
        void keyboardOpened();

        void keyboardClosed();

        void keyboardChangeState();
    }

    private void initVariables(Context context) {
        mContext = context;
        getScreenSize(context);
    }

    private int getScreenHeight() {
        return getScreenSize().y;
    }

    private Point getScreenSize() {
        return getScreenSize(mContext);
    }

    private Point getScreenSize(Context context) {
        return Utils.getSrceenSize(context);
    }

    private boolean isKeyboardOpenedOnStart(int height) {
        return getScreenHeight() * mLayoutSizeInPercent / 100 * (1 - (float) KEYBOARD_SIZE_IN_PERCENT / 100) > height;
    }

    @SuppressWarnings("unused")
    private boolean isKeyboardOpenedAfterStart(int height, int maxHeight) {
        return (float) height / maxHeight < (1 - (float) KEYBOARD_SIZE_IN_PERCENT / 100);
    }
}
