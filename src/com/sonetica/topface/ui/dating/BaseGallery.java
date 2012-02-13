package com.sonetica.topface.ui.dating;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public abstract class BaseGallery extends ViewGroup {
  // Data
  protected int   mCurrentScreen;  // текущее изображение в галереи
  private   int   mScrollX;        // x
  private   float mLastMotionX;    // last x
  private   int   mTouchState;     // скролинг или нажатие на итем
  private   int   mTouchSlop;      // растояние, пройденное пальцем, для определения скролинга
  private   Scroller mScroller;  
  private   VelocityTracker mVelocityTracker;
  // Constants
  private static final int SNAP_VELOCITY         = 1000;
  private static final int TOUCH_STATE_IDLE      = 0;
  private static final int TOUCH_STATE_SCROLLING = 1;
  //-------------------------------------------------------------------------
  public BaseGallery(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    mScroller   = new Scroller(context);
    mTouchState = TOUCH_STATE_IDLE;
    mTouchSlop  = ViewConfiguration.get(getContext()).getScaledTouchSlop() + 30;
    
    setBackgroundColor(Color.TRANSPARENT);
    setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
  }
  //-------------------------------------------------------------------------
  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    int action = ev.getAction();
    
    if((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_IDLE))
      return true;
    
    float x = ev.getX();
    
    switch(action) {
      case MotionEvent.ACTION_DOWN: {
        mLastMotionX = x;
        mTouchState  = mScroller.isFinished() ? TOUCH_STATE_IDLE : TOUCH_STATE_SCROLLING;
      } break;
      case MotionEvent.ACTION_MOVE: {
        int xDiff = (int)Math.abs(x - mLastMotionX);
        if(xDiff > mTouchSlop)
          mTouchState = TOUCH_STATE_SCROLLING;
      } break;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        mTouchState = TOUCH_STATE_IDLE;
        break;
    }
    
    return mTouchState != TOUCH_STATE_IDLE;
  }
  //-------------------------------------------------------------------------
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if(mVelocityTracker==null)
      mVelocityTracker = VelocityTracker.obtain();
    
    mVelocityTracker.addMovement(event);
    
    int   action  = event.getAction();
    float motionX = event.getX();

    switch(action) {
      case MotionEvent.ACTION_DOWN: {
        if(!mScroller.isFinished())
          mScroller.abortAnimation();
        mLastMotionX = motionX;
      } break;
      case MotionEvent.ACTION_MOVE: {
        int deltaX = (int)(mLastMotionX - motionX);
        mLastMotionX = motionX;
        if(deltaX < 0) {
          if(mScrollX > 0)
            scrollBy(Math.max(-mScrollX,deltaX),0);
        } else if(deltaX > 0) {
          int availableToScroll = getChildAt(getChildCount()-1).getRight()-mScrollX-getWidth();
          if(availableToScroll > 0)
            scrollBy(Math.min(availableToScroll,deltaX),0);
        }
      } break;
      case MotionEvent.ACTION_UP: {
        VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
        int velocityX = (int)velocityTracker.getXVelocity();
        if(velocityX > SNAP_VELOCITY && mCurrentScreen > 0)
          snapToScreen(mCurrentScreen - 1);
        else if(velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1)
          snapToScreen(mCurrentScreen + 1);
        else
          snapToDestination();

        if(mVelocityTracker != null) {
          mVelocityTracker.recycle();
          mVelocityTracker = null;
        }
        mTouchState = TOUCH_STATE_IDLE;
      } break;
      case MotionEvent.ACTION_CANCEL:
        mTouchState = TOUCH_STATE_IDLE;
    }

    mScrollX = this.getScrollX();
    
    return true;
  }
  //-------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(widthMeasureSpec,heightMeasureSpec);
    
    scrollTo(mCurrentScreen*width,0);
  }
  //-------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int l,int t,int r,int b) {
    int childLeft = 0;
    int count = getChildCount();
    for(int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if(child.getVisibility() != View.GONE) {
        final int childWidth = child.getMeasuredWidth();
        child.layout(childLeft,0,childLeft + childWidth,child.getMeasuredHeight());
        childLeft += childWidth;
      }
    }
  }
  //-------------------------------------------------------------------------
  @Override
  public void computeScroll() {
    if(mScroller.computeScrollOffset()) {
      mScrollX = mScroller.getCurrX();
      scrollTo(mScrollX, 0);
      postInvalidate();
    }
  }
  //-------------------------------------------------------------------------
  private void snapToDestination() {
    int screenWidth = getWidth();
    int whichScreen = (mScrollX+(screenWidth/2))/screenWidth;
    snapToScreen(whichScreen);
  }
  //-------------------------------------------------------------------------
  private void snapToScreen(int whichScreen) {
    mCurrentScreen = whichScreen;
    int newX  = whichScreen * getWidth();
    int delta = newX - mScrollX;
    mScroller.startScroll(mScrollX,0,delta,0,Math.abs(delta)*2);
    currentScreen(whichScreen);
    invalidate();
  }
  //-------------------------------------------------------------------------
  private void setToScreen(int whichScreen) {
    mCurrentScreen = whichScreen;
    int newX = whichScreen * getWidth();
    mScroller.startScroll(newX, 0, 0, 0, 10);
    invalidate();
  }
  //-------------------------------------------------------------------------
  abstract public void currentScreen(int whichScreen);
  //-------------------------------------------------------------------------
}
