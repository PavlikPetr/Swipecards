package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class SwapControl extends ViewGroup {
    // Data
    private int mScrollY;
    private Scroller mScroller;
    private OnSizeChangedListener mSizeChangedListener;

    public SwapControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
        getChildAt(1).measure(widthMeasureSpec, getChildAt(1).getMeasuredHeight());
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int childTop = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final int childHeight = child.getMeasuredHeight();
            child.layout(0, childTop, child.getMeasuredWidth(), childTop + childHeight);
            childTop += childHeight;
        }
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mScrollY = mScroller.getCurrY();
            scrollTo(0, mScrollY);
            postInvalidate();
        }
    }

    public void snapToScreen(int screenPosition, boolean instant) {
        int h = getChildAt(1).getHeight();
        if (screenPosition == 1)
            mScroller.startScroll(0, 0, 0, h, instant ? 0 : Math.abs(h) * 2);
        else
            mScroller.startScroll(0, h, 0, -h, instant ? 0 : Math.abs(h) * 2);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        mSizeChangedListener = listener;
    }

    public static interface OnSizeChangedListener {
        public void onSizeChanged(int w, int h, int oldw, int oldh);
    }
}
