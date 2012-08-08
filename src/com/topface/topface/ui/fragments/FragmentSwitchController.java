package com.topface.topface.ui.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

public class FragmentSwitchController extends ViewGroup implements View.OnClickListener {
    private int mScrollX;
    private int mPrevX;
    private int mDX;
    private int mFDX;
    private int mWidth;
    private int mAnimation;
    private Scroller mScroller;
    private FragmentSwitchListener mFragmentSwitchListener;
    private FragmentMenu mFragmentMenu;    
    
    private boolean mScrolling = false;
    
    public void setFragmentMenu(FragmentMenu fragmentMenu) {
        mFragmentMenu = fragmentMenu;
    }
    
    public static final int CLOSED = 0;
    public static final int EXPAND = 1;
    public static final int EXPAND_FULL = 2;
    public static final int COLLAPSE = 3;
    public static final int COLLAPSE_FULL = 4;
    
    public interface FragmentSwitchListener {
        public void endAnimation(int Animation);
        public void onSwitchStart();
    	public void onSwitchEnd();
    }    

    public FragmentSwitchController(Context context, AttributeSet attrs) {
        super(context, attrs);        
        mScroller =  new Scroller(context, new AccelerateInterpolator(1.0f)); //new Scroller(context, new DecelerateInterpolator(1.0f));
        
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getChildAt(0).measure(getChildAt(0).getMeasuredWidth(), heightMeasureSpec); // shadow
        getChildAt(1).measure(widthMeasureSpec, heightMeasureSpec); // fragments
        
        getChildAt(1).setOnClickListener(this);
    }

    @Override
    protected void onLayout(boolean changed,int l,int t,int r,int b) {
        getChildAt(0).layout(-getChildAt(0).getMeasuredWidth(), 0, 0, getChildAt(0).getMeasuredHeight());
        getChildAt(1).layout(0, 0, getChildAt(1).getMeasuredWidth(), getChildAt(1).getMeasuredHeight());
        
        int x = mFragmentMenu.getView().getMeasuredWidth();
        
        mWidth = getChildAt(1).getWidth();
        mDX = mWidth-(mWidth/100*20);
        mFDX = mWidth-mDX;
    }

    @Override
    public void computeScroll() {
        mScrollX = mScroller.getCurrX();
        if (!mScroller.isFinished()) {
	        if (mScroller.computeScrollOffset()) {
	            scrollTo(mScrollX, 0);
	            postInvalidate();
	            return;
	        }
        }
                
        endAnimation();
    }

    public void snapToScreen(int typeAnimation) {
        mAnimation = typeAnimation;
        setScrollingCacheEnabled(true);
        mScrolling = true;      
        mFragmentSwitchListener.onSwitchStart();
        switch (typeAnimation) {
            case EXPAND:                	            	
                mScroller.startScroll(mPrevX, 0, -mDX, 0, 300);
                break;
            case COLLAPSE:
                mScroller.startScroll(mPrevX, 0, mDX, 0, 300);
                break;
            case EXPAND_FULL:
                mScroller.startScroll(mPrevX, 0, -(mFDX), 0, 300);
                break;
            case COLLAPSE_FULL:
                mScroller.startScroll(mPrevX, 0, mWidth, 0, 300);
                break;
            default:
                break;
        }
        invalidate();
    }
    
    public void setScrollingCacheEnabled(boolean enabled) {
        getChildAt(0).setDrawingCacheEnabled(enabled);
        getChildAt(1).setDrawingCacheEnabled(enabled);
        //buildDrawingCache(enabled);
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
	    mScroller.abortAnimation();
		int oldX = getScrollX();
		int x = mScroller.getCurrX();
		mPrevX = x;
		if (oldX != x) {
			scrollTo(x, 0);	
		}
		
		if (mScrolling) {			
			mScrolling = false;
			if(mAnimation != EXPAND_FULL) {
				mFragmentSwitchListener.onSwitchEnd();				
			}
			if(mFragmentSwitchListener != null)
				mFragmentSwitchListener.endAnimation(mAnimation);			
		}
		
		setScrollingCacheEnabled(false);
    }

    public void setFragmentSwitchListener(FragmentSwitchListener fragmentSwitchListener) {
        mFragmentSwitchListener = fragmentSwitchListener;
    }    
    
    @Override
    public void onClick(View v) {
    }
    
}

