package com.topface.topface.ui.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Scroller;

public class FragmentSwitchController extends ViewGroup {
    private int mScrollX;
    private int mPrevX;
    private int mDX;
    private int mWidth;
    private int mAnimation;
    private Scroller mScroller;
    private FragmentSwitchListener mFragmentSwitchListener;
    
    public static final int CLOSED = 0;
    public static final int EXPAND = 1;
    public static final int EXPAND_FULL = 2;
    public static final int COLLAPSE = 3;
    public static final int COLLAPSE_FULL = 4;
    
    public interface FragmentSwitchListener {
        public void endAnimation(int Animation);
    }

    public FragmentSwitchController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec); // left
        getChildAt(1).measure(widthMeasureSpec, heightMeasureSpec); // right
    }

    @Override
    protected void onLayout(boolean changed,int l,int t,int r,int b) {
        getChildAt(0).layout(-getChildAt(0).getMeasuredWidth(), 0, 0, getChildAt(0).getMeasuredHeight());
        getChildAt(1).layout(0, 0, getChildAt(0).getMeasuredWidth(), getChildAt(0).getMeasuredHeight());
        
        mWidth = getChildAt(1).getWidth();
        mDX = mWidth-(mWidth/100*20);
    }

    @Override
    public void computeScroll() {
        mScrollX = mScroller.getCurrX();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScrollX, 0);
            postInvalidate();
        } else if (mPrevX != mScrollX) {
            mPrevX = mScrollX;
            endAnimation();              
        }
    }

    public void snapToScreen(int typeAnimation) {
        mAnimation = typeAnimation;
        switch (typeAnimation) {
            case EXPAND:
                mScroller.startScroll(mPrevX, 0, -mDX, 0, 200);
                break;
            case COLLAPSE:
                mScroller.startScroll(mPrevX, 0, mDX, 0, 200);
                break;
            case EXPAND_FULL:
                mScroller.startScroll(mPrevX, 0, -(mWidth-mDX), 0, 50);
                break;
            case COLLAPSE_FULL:
                mScroller.startScroll(mPrevX, 0, mWidth, 0, 100);
                break;
            default:
                break;
        }
        invalidate();        
    }

    public void openMenu() {
        snapToScreen(EXPAND);
    }
    
    public void closeMenu() {
        snapToScreen(COLLAPSE);
    }
    
    public int getAnimationState() {
        return mAnimation;  
    }

    public void endAnimation() {
        if(mFragmentSwitchListener != null)
            mFragmentSwitchListener.endAnimation(mAnimation);
    }

    public void setFragmentSwitchListener(FragmentSwitchListener fragmentSwitchListener) {
        mFragmentSwitchListener = fragmentSwitchListener;
    }
    
}

