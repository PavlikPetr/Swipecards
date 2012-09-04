package com.topface.topface.ui.fragments;

import com.topface.topface.ui.views.DatingAlbum;
import com.topface.topface.utils.Debug;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FragmentSwitchController extends ViewGroup implements View.OnClickListener {
	private int mScrollX;
	private int mOpenDX;
	private int mFullOpenDX;
	private int mWidth;
	private int mAnimation;
	private Scroller mScroller;
	private FragmentSwitchListener mFragmentSwitchListener;
	private FragmentMenu mFragmentMenu;
	
	private int mMinimumVelocity;
	private int mVelocitySlop;
	private boolean mAutoScrolling = false;
	
	public static final int EXPANDING_PERCENT = 30;

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
		
		public void onOpenStart();
	}

	private static final Interpolator prixingInterpolator = new Interpolator() {
		public float getInterpolation(float t) {			
			return (t-1)*(t-1)*(t-1)*(t-1)*(t-1) + 1.0f;
		}
	};
	
	public FragmentSwitchController(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context, prixingInterpolator);//new AccelerateInterpolator(1.0f)); // new
																				// Scroller(context,
																				// new
																				// DecelerateInterpolator(1.0f));

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mMinimumVelocity = 10*configuration.getScaledMinimumFlingVelocity();
		mVelocitySlop = configuration.getScaledMinimumFlingVelocity();
		mTouchSlop = ViewConfiguration.getTouchSlop();
		mAnimation = COLLAPSE;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		getChildAt(0).measure(getChildAt(0).getMeasuredWidth(), heightMeasureSpec); // shadow
		getChildAt(1).measure(widthMeasureSpec, heightMeasureSpec); // fragments

		getChildAt(1).setOnClickListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		getChildAt(0).layout(-getChildAt(0).getMeasuredWidth(), 0, 0,
				getChildAt(0).getMeasuredHeight());
		getChildAt(1).layout(0, 0, getChildAt(1).getMeasuredWidth(),
				getChildAt(1).getMeasuredHeight());
		
		mWidth = getChildAt(1).getWidth();
		mOpenDX = mWidth - (mWidth / 100 * EXPANDING_PERCENT);
		mFullOpenDX = mWidth - mOpenDX;
		mScrollingDistanceThreshold = mWidth / 6;
		
	    mFragmentMenu.setNotificationMargin(mOpenDX/100*90);
	    //mFragmentMenu.refreshNotifications();
	}

	private int getLeftBound() {
		return 0;
	}
	
	private int getRightBound() {
		return mOpenDX;
	}
	
	private int counter = 0;
	
	@Override
	public void computeScroll() {
		Debug.log("computeScroll(): "+ (++counter));
		mScrollX = mScroller.getCurrX();
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				scrollTo(mScrollX, getScrollY());				
				return;
			}
		}

		endScrollAnimation();
	}

	public void snapToScreen(int typeAnimation) {
		counter = 0;
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
		// buildDrawingCache(enabled);
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

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);	
		invalidate();
	}
	
	float mLastMotionX;
	float mLastMotionY;
	float mTouchSlop;
	boolean mIsDragging = false;
	private VelocityTracker mVelocityTracker;
	private float mMaximumVelocity;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		Log.d("OLOLO","Inter");
		
		if (mAutoScrolling) 
			return false;
		
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();		

		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}		
				
		if (!inBezierThreshold(x) && (mAnimation == COLLAPSE || mAnimation == COLLAPSE_FULL))
			return false;
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:			
			if (mAnimation == EXPAND) {
				startDragging(x);
				break;
			}
			
			float dx = x - mLastMotionX;
			float xDiff = Math.abs(dx);
			float dy = y - mLastMotionY;
			float yDiff = Math.abs(dy);
			
			if (canScroll(getChildAt(1), false, (int) dx, (int) x, (int) y)) {
				return false;				
			}
			
			if(xDiff > mTouchSlop && xDiff > yDiff) {
				startDragging(x);
				if (mAnimation == COLLAPSE || mAnimation == COLLAPSE_FULL) {
					mFragmentSwitchListener.onOpenStart();
				}
			} else {
				stopDragging(x);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			stopDragging(x);
			break;
		case MotionEvent.ACTION_UP:
			if (mAnimation == EXPAND) { 
				completeDragging(0);				
				return true;
			}
			stopDragging(x);
			break;
		}
		
		return mIsDragging;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("OLOLO","Touch");		
		
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		
		if (!mIsDragging) {
			return false;
		} else {
			mVelocityTracker.addMovement(event);
		}
		
		int action = event.getAction();
		float x = event.getX();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mVelocityTracker.addMovement(event);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsDragging) {		
				float dx = mLastMotionX - x;
				mLastMotionX = x;
				
				final float oldXScroll = getScrollX();
				float newXScroll = oldXScroll + dx;
				
				if (-newXScroll < getLeftBound()) {
					newXScroll = -getLeftBound();
				} else if(-newXScroll > getRightBound()) {
					newXScroll = -getRightBound();
				}
				
				scrollTo((int)newXScroll, getScrollY());
			}
			break;
		case MotionEvent.ACTION_UP:
			final VelocityTracker velocityTracker = mVelocityTracker; 
			velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);			
			int initialVelocity = (int) velocityTracker.getXVelocity();			
			completeDragging(initialVelocity);
			break;
		case MotionEvent.ACTION_CANCEL:
			stopDragging(x);
			break;		
		}

		return true;
	}

	private void startDragging(float x) {
		mIsDragging = true;		
		mLastMotionX = x;
		setScrollingCacheEnabled(true);
	}

	private void stopDragging(float x) {
		mIsDragging = false;
		setScrollingCacheEnabled(false);
	}	
	
	private int mScrollingVelocityThreshold = 2000;	
	private float mScrollingDistanceThreshold;
	
	private void completeDragging(int velocity) {
		Log.d("OLOLO","X Velocity = " + velocity + " Scroll = " + getScrollX());
		
		mIsDragging = false;
		mAutoScrolling = true;
		int dx = 0;
		int duration = 0;		
		
		if(Math.abs(velocity) < mVelocitySlop) 
			velocity = 0;
		
		if(velocity > 0) {			
			// right - expect EXPAND
			dx = -getRightBound() - getScrollX();
			
			if (velocity >= mScrollingVelocityThreshold) {
				//EXPAND because user slides insanely
				mAnimation = EXPAND;
				duration = (int) Math.abs(1000*dx/mScrollingVelocityThreshold);
			} else {
				if (velocity < mMinimumVelocity) {
					velocity = mMinimumVelocity;
				}
				//EXPAND with normal velocity, but check "distance threshold"
				if (-getScrollX() - getLeftBound() > mScrollingDistanceThreshold) {
					mAnimation = EXPAND;
				} else {
					mAnimation = COLLAPSE;
					dx = -getScrollX() - getLeftBound();
				}
				duration = (int) Math.abs(1000*dx/velocity);
			}
			mScroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY(), duration);
		} else if (velocity < 0){
			// left - expect COLLAPSE
			dx = -getScrollX() - getLeftBound();
			
			if (-velocity >= mScrollingVelocityThreshold) {
				duration = (int) Math.abs(1000*dx/mScrollingVelocityThreshold);
			} else {
				if (-velocity < mMinimumVelocity) {
					velocity = mMinimumVelocity;
				}
				duration = (int) Math.abs(1000*dx/velocity);
			}
			
			mAnimation = COLLAPSE;		
			mScroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY(), duration);
		} else {			
			if (-getScrollX() > mScrollingDistanceThreshold) {
				if (mAnimation == EXPAND) {
					mAnimation = COLLAPSE;
					dx = -getScrollX() - getLeftBound();
				} else {
					mAnimation = EXPAND;
					dx = -getRightBound() - getScrollX();
				}				
			} else {
				mAnimation = COLLAPSE;
				dx = -getScrollX() - getLeftBound();
			}
						
			duration = (int) Math.abs(1000*dx/mMinimumVelocity);
			
			mScroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY(), duration);
		}
		mScroller.computeScrollOffset();
		invalidate();
	}
	
	/**
	 * Tests scrollability within child views of v given a delta of dx.
	 * 
	 * @param v
	 *            View to test for horizontal scrollability
	 * @param checkV
	 *            Whether the view v passed should itself be checked for
	 *            scrollability (true), or just its children (false).
	 * @param dx
	 *            Delta scrolled in pixels
	 * @param x
	 *            X coordinate of the active touch point
	 * @param y
	 *            Y coordinate of the active touch point
	 * @return true if child views of v can be scrolled by delta of dx.
	 */
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {				
		if (v instanceof ViewGroup) {
			final ViewGroup group = (ViewGroup) v;
			if (group.isShown()) {
				final int scrollX = v.getScrollX();
				final int scrollY = v.getScrollY();
				final int count = group.getChildCount();
				for (int i = count - 1; i >= 0; i--) {
					final View child = group.getChildAt(i);
					if (x + scrollX >= child.getLeft()
							&& x + scrollX < child.getRight()
							&& y + scrollY >= child.getTop()
							&& y + scrollY < child.getBottom()
							&& canScroll(child, true, dx, x + scrollX - child.getLeft(), y + scrollY
									- child.getTop())) {
						return true;
					}
				}
			}
		}

		boolean result = false;		
		
		//for API versions < 14
		if (v instanceof DatingAlbum) {
			result = ((DatingAlbum)v).canScrollHorizontally(-dx);
		//for API versions >= 14 (ICS)
		} else {
			result = ViewCompat.canScrollHorizontally(v, -dx);
		}
		
		return checkV && result;
	}
	
	protected boolean inBezierThreshold(float x) {
		if (x < mWidth/5) {
			return true;
		}
		return false;
	}
}
