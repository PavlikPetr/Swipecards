package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FragmentSwitcher extends ViewGroup implements View.OnClickListener {
    
	private int mScrollX;
	private int mOpenDX;
	private int mClosedDX;
	private int mFullOpenDX;
	private int mWidth;
	private int mAnimation;
	private int mCurrentFragmentId;
	private Scroller mScroller;
	private FragmentManager mFragmentManager;
	private FragmentSwitchListener mFragmentSwitchListener;

	private boolean mAutoScrolling = false;
	
    public static final int CLOSED = 0;
	public static final int EXPAND = 1;
	public static final int EXPAND_FULL = 2;
	public static final int COLLAPSE = 3;
	public static final int COLLAPSE_FULL = 4;
	
	public static final int EXPANDING_PERCENT = 30;

	private final Interpolator mPrixingInterpolator = new Interpolator() {
		public float getInterpolation(float t) {			
			return (t-1)*(t-1)*(t-1)*(t-1)*(t-1) + 1.0f;
		}
	};
	
	/*
     *   interface FragmentSwitchListener
     */
    public interface FragmentSwitchListener {
        public void endAnimation(int Animation);
        public void onSwitchStart();
        public void onSwitchEnd();
        public void onOpenStart();
    }
    
    public FragmentSwitcher(Context context) {
        this(context, null);
    }
	
	public FragmentSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCurrentFragmentId = BaseFragment.F_PROFILE;
		mScroller = new Scroller(context, mPrixingInterpolator);
	}
	
    public void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }
	
    public void showFragment(int fragmentId) {
        mCurrentFragmentId = fragmentId;
        BaseFragment fragment;
        switch (fragmentId) {
            case BaseFragment.F_PROFILE:
                fragment = new ProfileFragment();
                break;
            case BaseFragment.F_DATING:
                fragment = new DatingFragment();
                break;
            case BaseFragment.F_LIKES:
                fragment = new LikesFragment();
                break;
            case BaseFragment.F_MUTUAL:
                fragment = new MutualFragment();
                break;
            case BaseFragment.F_DIALOGS:
                fragment = new DialogsFragment();
                break;
            case BaseFragment.F_TOPS:
                fragment = new TopsFragment();
                break;
            case BaseFragment.F_SETTINGS:
                fragment = new SettingsFragment();
                break; 
            default:
                fragment = new ProfileFragment();
                break;
        }
        mFragmentManager.beginTransaction().replace(R.id.fragment_fragment, fragment).commit();        
    }
    
    public int getCurrentFragmentId() {
        return mCurrentFragmentId;
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		 // shadow
		getChildAt(0).measure(getChildAt(0).getMeasuredWidth(), heightMeasureSpec);
		// fragments
		getChildAt(1).measure(widthMeasureSpec, heightMeasureSpec); 
		getChildAt(1).setOnClickListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		getChildAt(0).layout(-getChildAt(0).getMeasuredWidth(), 0, 0, getChildAt(0).getMeasuredHeight());
		getChildAt(1).layout(0, 0, getChildAt(1).getMeasuredWidth(), getChildAt(1).getMeasuredHeight());
		
		mWidth = getChildAt(1).getWidth();
		mClosedDX = mWidth / 100 * EXPANDING_PERCENT;
		mOpenDX = mWidth - mClosedDX;
		mFullOpenDX = mWidth - mOpenDX;
	}

	@Override
	public void computeScroll() {
		mScrollX = mScroller.getCurrX();
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				scrollTo(mScrollX, getScrollY());				
				return;
			}
		}
		endScrollAnimation();
	}
	
   @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);   
        invalidate();
    }

	public void snapToScreen(int typeAnimation) {
		mAnimation = typeAnimation;
		setScrollingCacheEnabled(true);
		mAutoScrolling = true;
		mFragmentSwitchListener.onSwitchStart();
		switch (typeAnimation) {
    		case EXPAND:
    			mFragmentSwitchListener.onOpenStart();
    			mScroller.startScroll(getLeftBound(), 0, -getRightBound(), 0, 300);
    			break;
    		case COLLAPSE:
    			mScroller.startScroll(-getRightBound(), 0, getRightBound(), 0, 300);
    			break;
    		case EXPAND_FULL:
    			mScroller.startScroll(-getRightBound(), 0, -(mFullOpenDX), 0, 300);
    			break;
    		case COLLAPSE_FULL:
    			mScroller.startScroll(-mWidth, 0, mWidth, 0, 300);
    			break;
    		default:
    			break;
		}
		invalidate();
	}

	public void setScrollingCacheEnabled(boolean enabled) {
		getChildAt(0).setDrawingCacheEnabled(enabled);
		getChildAt(1).setDrawingCacheEnabled(enabled);
	}
	
   private int getLeftBound() {
        return 0;
    }
    
    private int getRightBound() {
        return mOpenDX;
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

	public void endScrollAnimation() {		
		if (mAutoScrolling) {
			mScroller.abortAnimation();
			int oldX = getScrollX();
			int x = mScroller.getCurrX();
			if (oldX != x) {
				scrollTo(x, 0);
			}
		
			mAutoScrolling = false;			
			if (mAnimation != EXPAND_FULL) {
				mFragmentSwitchListener.onSwitchEnd();
			}
			if (mFragmentSwitchListener != null)
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
