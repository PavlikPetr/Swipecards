package com.sonetica.topface.ui.profile;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class SwapView extends ViewGroup {
  //-------------------------------------------------------------------------
  // interface OnSwapListener
  //-------------------------------------------------------------------------
  public interface OnSwapListener {
    public void onSwap();
  }
  //-------------------------------------------------------------------------
  // Data
  protected int   mCurrentScreen;  // текущее изображение в галереи
  private   int   mScrollY;        // x
  private   float mLastMotionY;    // last x
  private   int   mTouchState;     // скролинг или нажатие на итем
  private   int   mTouchSlop;      // растояние, пройденное пальцем, для определения скролинга
  private   Scroller mScroller;  
  private   VelocityTracker mVelocityTracker;
  private   OnSwapListener  mSwapListener;
  // Constants
  private static final int SNAP_VELOCITY         = 1000;
  private static final int TOUCH_STATE_IDLE      = 0;
  private static final int TOUCH_STATE_SCROLLING = 1;
  //-------------------------------------------------------------------------
  public SwapView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    mScroller   = new Scroller(context);
    mTouchState = TOUCH_STATE_IDLE;
    mTouchSlop  = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    
    setBackgroundColor(Color.TRANSPARENT);
    setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
  }
  //-------------------------------------------------------------------------
  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {

    int action = ev.getAction();
    
    if((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_IDLE))
      return true;
    
    float y = ev.getY();
    
    switch(action) {
      case MotionEvent.ACTION_DOWN: {
        mLastMotionY = y;
        mTouchState  = mScroller.isFinished() ? TOUCH_STATE_IDLE : TOUCH_STATE_SCROLLING;
      } break;
      case MotionEvent.ACTION_MOVE: {
        int yDiff = (int)Math.abs(y - mLastMotionY);
        if(yDiff > mTouchSlop)
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
    float motionY = event.getY();

    switch(action) {
      case MotionEvent.ACTION_DOWN: {
        if(!mScroller.isFinished())
          mScroller.abortAnimation();
        mLastMotionY = motionY;
      } break;
      case MotionEvent.ACTION_MOVE: {
        int deltaY = (int)(mLastMotionY - motionY);
        mLastMotionY = motionY;
        if(deltaY < 0) {
          if(mScrollY > 0)
            scrollBy(0,Math.max(-mScrollY,deltaY));
        } else if(deltaY > 0) {
          int availableToScroll = getChildAt(getChildCount()-1).getBottom()-mScrollY-getHeight();
          if(availableToScroll > 0)
            scrollBy(0,Math.min(availableToScroll,deltaY));
        }
      } break;
      case MotionEvent.ACTION_UP: {
        VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
        int velocityY = (int)velocityTracker.getYVelocity();
        if(velocityY > SNAP_VELOCITY && mCurrentScreen > 0)
          snapToScreen(mCurrentScreen - 1);
        else if(velocityY < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1)
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

    mScrollY = this.getScrollY();
    
    return true;
  }
  //-------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    //int width  = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int count  = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(widthMeasureSpec,heightMeasureSpec);
    
    scrollTo(0,mCurrentScreen*height);
  }
  //-------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int l,int t,int r,int b) {
    int count = getChildCount();
    int childTop = 0;
    for(int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if(child.getVisibility() != View.GONE) {
        final int childHeight = child.getMeasuredHeight();
        child.layout(0,childTop,child.getMeasuredWidth(),childTop + childHeight);
        childTop += childHeight;
      }
    }
  }
  //-------------------------------------------------------------------------
  @Override
  public void computeScroll() {
    if(mScroller.computeScrollOffset()) {
      mScrollY = mScroller.getCurrY();
      scrollTo(0,mScrollY);
      postInvalidate();
    }
  }
  //-------------------------------------------------------------------------
  private void snapToDestination() {
    int screenHeight = getHeight();
    int heightScreen = (mScrollY+(screenHeight/2))/screenHeight;
    snapToScreen(heightScreen);
  }
  //-------------------------------------------------------------------------
  public void snapToScreen(int heightScreen) {
    if(mCurrentScreen == heightScreen)
      return;
    mCurrentScreen = heightScreen;
    int newY  = heightScreen * getHeight();
    int delta = newY - mScrollY;
    mScroller.startScroll(0,mScrollY,0,delta,Math.abs(delta)*2);
    currentScreen(heightScreen);
    invalidate();
  }
  //-------------------------------------------------------------------------
  /*
  private void setToScreen(int whichScreen) {
    mCurrentScreen = whichScreen;
    int newX = whichScreen * getWidth();
    mScroller.startScroll(newX, 0, 0, 0, 10);
    invalidate();
  }
  */
  //-------------------------------------------------------------------------
  public void currentScreen(int heightScreen) {
    //if(mCurrentScreen!=heightScreen)
      mSwapListener.onSwap();    
  }
  //-------------------------------------------------------------------------
  public void setOnSwapListener(OnSwapListener listener) {
    mSwapListener = listener;
  } 
  //-------------------------------------------------------------------------
}
