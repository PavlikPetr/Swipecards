package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class FragmentSwitchController extends ViewGroup implements View.OnClickListener {
    private int mScrollX;
    private int mPrevX;
    private int mDX;
    private int mWidth;
    private int mAnimation;
    private int mFragmentId;
    private Scroller mScroller;
    private FragmentFrameAdapter mFragmentFrameAdapter;
    
    public static final int EXPAND = 1;
    public static final int EXPAND_FULL = 2;
    public static final int COLLAPSE = 3;
    public static final int COLLAPSE_FULL = 4;

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
        mDX = mWidth-(mWidth/100*30);
    }

    @Override
    public void computeScroll() {
        mScrollX = mScroller.getCurrX();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScrollX, 0);
            postInvalidate();
        } else if (mPrevX != mScrollX) {
            mPrevX = mScrollX;
            endAnimation(mAnimation);              
        }
    }

    public void snapToScreen(int typeAnimation) {
        mAnimation = typeAnimation;
        switch (typeAnimation) {
            case EXPAND:
                mScroller.startScroll(mPrevX, 0, -mDX, 0, 250);
                break;
            case COLLAPSE:
                mScroller.startScroll(mPrevX, 0, mDX, 0, 250);
                break;
            case EXPAND_FULL:
                mScroller.startScroll(mPrevX, 0, -(mWidth-mDX), 0, 250);
                break;
            case COLLAPSE_FULL:
                mScroller.startScroll(mPrevX, 0, mWidth, 0, 250);
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

    public void endAnimation(int Animation) {
        if(Animation == EXPAND_FULL) {
            snapToScreen(COLLAPSE_FULL);
            mFragmentFrameAdapter.showFragment(mFragmentId);
        }
    }
    
    public void setFragmentFrameAdapter(FragmentFrameAdapter frameAdapter) {
        mFragmentFrameAdapter = frameAdapter;        
    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFragmentProfile:
                mFragmentId = R.id.fragment_profile;
                break;
            case R.id.btnFragmentDating:
                mFragmentId = R.id.fragment_dating;
                break;
            case R.id.btnFragmentLikes:
                mFragmentId = R.id.fragment_likes;
                break;
            case R.id.btnFragmentMutual:
                mFragmentId = R.id.fragment_mutual;
                break;
            case R.id.btnFragmentDialogs:
                mFragmentId = R.id.fragment_dialogs;
                break;
            case R.id.btnFragmentTops:
                mFragmentId = R.id.fragment_tops;
                break;
            case R.id.btnFragmentSettings:
                mFragmentId = R.id.fragment_settings;
                break; 
            default:
                break;
        }
        snapToScreen(EXPAND_FULL);
    }
}

