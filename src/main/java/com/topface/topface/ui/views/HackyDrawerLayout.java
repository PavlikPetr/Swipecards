package com.topface.topface.ui.views;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * fix ArrayIndexOutOfBoundsException (https://github.com/chrisbanes/PhotoView/issues/72)
 */
@SuppressWarnings("UnusedDeclaration")
public class HackyDrawerLayout extends DrawerLayout {

    private IBackPressedListener mBackPressedListener;

    public HackyDrawerLayout(Context context) {
        super(context);
    }

    public HackyDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HackyDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBackPressedListener != null) {
                mBackPressedListener.onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setBackPressedListener(IBackPressedListener listener) {
        mBackPressedListener = listener;
    }

    public interface IBackPressedListener {
        void onBackPressed();
    }
}
