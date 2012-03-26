package com.sonetica.topface.ui.dating;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;

public class DatingGallery extends Gallery implements View.OnTouchListener {
  // Data
  //private float   mStartX = -1;
  //private float[] mLastXs = new float[EVENT_COUNT];
  //private static final int EVENT_COUNT = 3;
  //---------------------------------------------------------------------------
  public DatingGallery(Context context, AttributeSet attrs) {
    super(context, attrs);
    //this.setOnTouchListener(this);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    float velMax = 2500f;
    float velMin = 1000f;
    float velX = Math.abs(velocityX);
    if (velX > velMax) {
      velX = velMax;
    } else if (velX < velMin) {
      velX = velMin;
    }
    velX -= 600;
    int k = 500000;
    int speed = (int) Math.floor(1f / velX * k);
    setAnimationDuration(speed);

    int kEvent;
    if (isScrollingLeft(e1, e2)) {
      // Check if scrolling left
      kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
    } else {
      // Otherwise scrolling right
      kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
    }
    onKeyDown(kEvent, null);

    return true;
  }
  //---------------------------------------------------------------------------
  private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2){ 
    return e2.getX() > e1.getX(); 
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    return false;
  }
  //---------------------------------------------------------------------------
  //public static final int LEFT  = -1;
  //public static final int RIGHT = 1;
  //public int side=0;
  //@Override
  //public boolean onTouch(View v,MotionEvent event) {
    /*
    if(this.getSelectedItemId()==0 || this.getSelectedItemId()==this.getCount()-1)
      switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE : {
          for (int i = 0; i < EVENT_COUNT - 1; i++)
            mLastXs[i] = mLastXs[i + 1];
          mLastXs[EVENT_COUNT - 1] = event.getX();

          side = mLastXs[0]>mLastXs[EVENT_COUNT-1] ? RIGHT : LEFT;
          
            if(this.getSelectedItemId()==0 && side==RIGHT){
              if(mStartX == -1)
                mStartX = event.getX();
              return false;
            }
              
            if(this.getSelectedItemId()==this.getCount()-1 && side==LEFT) {
              if(mStartX == -1)
                mStartX = event.getX();
              return false;
            }
          
          if(mLastXs[0] != 0 && mLastXs[EVENT_COUNT-1] != 0 && Math.abs(mLastXs[0]-mLastXs[EVENT_COUNT-1]) > 30 && mStartX == -1) {
            if(mStartX == -1)
              mStartX = event.getX();
            return false;
          }
          
          if(mStartX != -1 && !this.isPressed()) {
            float averageX=0;
            for(int i = 0; i < EVENT_COUNT; i++)
              averageX += mLastXs[i];
            averageX /= EVENT_COUNT;
            
            int height = 0;
            int p = 0;
            if(side==LEFT) {
              height = (int)(Math.max(averageX-mStartX, 0));
              p = (int)(height)*-1;
            }
            if(side==RIGHT) {
               height = (int)(Math.max(mStartX-averageX, 0));
               p = (int)(height)*1;
            }


            
            scrollTo(p,0);
            return true;
          }
        } break;
        case MotionEvent.ACTION_UP : {
          mStartX = -1;
          scrollTo(0,0);           
          for(int i = 0; i < EVENT_COUNT; i++)
            mLastXs[i] = 0;
        } break;
      }
      */
    //return false;
  //}
  //---------------------------------------------------------------------------
}

/*
if(this.getSelectedItemId()==0)
switch (event.getAction()) {
  case MotionEvent.ACTION_MOVE : {
    for (int i = 0; i < EVENT_COUNT - 1; i++)
      mLastXs[i] = mLastXs[i + 1];
    mLastXs[EVENT_COUNT - 1] = event.getX() + this.getTop();
    if(mLastXs[0] != 0 && mLastXs[EVENT_COUNT-1] != 0 && Math.abs(mLastXs[0]-mLastXs[EVENT_COUNT-1]) > 8 && mLastXs[0] < mLastXs[EVENT_COUNT-1] && mStartX == -1) {
      if(mStartX == -1)
        mStartX = event.getX();
      return false;
    }
    if(mStartX != -1 && !this.isPressed()) {
      float averageX=0;
      for(int i = 0; i < EVENT_COUNT; i++)
        averageX += mLastXs[i];
      averageX /= EVENT_COUNT;
      int height = (int)(Math.max(averageX-mStartX, 0));
      
      //int p = (int)(height)*( this.getSelectedItemId()==0?-1:1);
      int p = (int)(height)*-1;
      
      scrollTo(p,0);
      return true;
    }
  } break;
  case MotionEvent.ACTION_UP : {
    mStartX = -1;
    scrollTo(0,0);           
    for(int i = 0; i < EVENT_COUNT; i++)
      mLastXs[i] = 0;
  } break;
}
return false;
*/