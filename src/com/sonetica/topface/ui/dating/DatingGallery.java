package com.sonetica.topface.ui.dating;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.ZoomControls;

public class DatingGallery extends ViewGroup {
  // interface DatingEventListener
  interface DatingEventListener {
    public void openProfileActivity(int userId);
    public void openChatActivity(int userId);
    public void onRate(int userId,int index);
  }
  //---------------------------------------------------------------------------
  // Data
  private int   mScrollX;        // x
  private float mLastMotionX;    // last x
  private int   mCurrentScreen;  // текущее изображение в галереи
  private int   mTouchState;     // скролинг или нажатие на итем
  private int   mTouchSlop;      // растояние, пройденное пальцем, для определения скролинга
  private Scroller mScroller;  
  private VelocityTracker mVelocityTracker;
  private ImageView iv1;
  private ImageView iv2;
  private DatingLayout mDatingLayout;
  private DatingGalleryAdapter mAdapter;
  private DatingEventListener mEventListener;
  private ExecutorService mThreadsPool;
  private SearchUser mUser;
  // Constants
  private static final int SNAP_VELOCITY         = 1000;
  private static final int TOUCH_STATE_IDLE      = 0;
  private static final int TOUCH_STATE_SCROLLING = 1;
  //-------------------------------------------------------------------------
  public DatingGallery(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    mScroller   = new Scroller(context);
    mTouchState = TOUCH_STATE_IDLE;
    mTouchSlop  = ViewConfiguration.get(getContext()).getScaledTouchSlop()+40;
    
    mThreadsPool = Executors.newFixedThreadPool(1);
    
    setBackgroundColor(Color.TRANSPARENT);
    setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
    
    // фрейм оценок
    mDatingLayout = new DatingLayout(context);
    addView(mDatingLayout);
    
    // 1
    iv1 = new ImageView(context);
    iv1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // ДУБЛЯЖ
        int visibility = DatingActivity.mHeaderBar.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
        DatingActivity.mHeaderBar.setVisibility(visibility);
      }
    });
    addView(iv1);
    // 2
    iv2 = new ImageView(context);
    iv2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // ДУБЛЯЖ
        int visibility = DatingActivity.mHeaderBar.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
        DatingActivity.mHeaderBar.setVisibility(visibility);
      }
    });
    addView(iv2);
    
    
    mDatingLayout.mInfoView.setVisibility(View.INVISIBLE);
    mDatingLayout.mFaceView.setVisibility(View.INVISIBLE);
    mDatingLayout.mProgress.setVisibility(View.VISIBLE);
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
        if(velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
          snapToScreen(mCurrentScreen - 1);
          
          // показать звезды при возвращении на окно оценок !!!!!!!!!
          if(mCurrentScreen==0)
            mDatingLayout.hideChildren(View.VISIBLE);
          
        } else if(velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
          snapToScreen(mCurrentScreen + 1);
        } else
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
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(widthMeasureSpec,heightMeasureSpec);
    
    scrollTo(mCurrentScreen*width,0);
  }
  //-------------------------------------------------------------------------
  private void snapToDestination() {
    int screenWidth = getWidth();
    int whichScreen = (mScrollX+(screenWidth/2))/screenWidth;
    snapToScreen(whichScreen);
  }
  //-------------------------------------------------------------------------
  public void snapToScreen(int whichScreen) {
    mCurrentScreen = whichScreen;
    int newX  = whichScreen * getWidth();
    int delta = newX - mScrollX;
    mScroller.startScroll(mScrollX,0,delta,0,Math.abs(delta)*2);             
    invalidate();
  }
  //-------------------------------------------------------------------------
  public void setToScreen(int whichScreen) {
    mCurrentScreen = whichScreen;
    int newX = whichScreen * getWidth();
    mScroller.startScroll(newX, 0, 0, 0, 10);             
    invalidate();
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
  //---------------------------------------------------------------------------
  public void setAdapter(DatingGalleryAdapter adapter) {
    mAdapter = adapter;
  }
  //---------------------------------------------------------------------------
  public void setEventListener(DatingEventListener eventListener) {
    mEventListener = eventListener;
  }
  //---------------------------------------------------------------------------
  public void notifyDataChanged() {
    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        Looper.prepare();
        mUser = mAdapter.getUser();
        
        mDatingLayout.mInfoView.age    = mUser.age;
        mDatingLayout.mResView.power  = Data.s_Power;
        mDatingLayout.mResView.money  = Data.s_Money;
        mDatingLayout.mInfoView.city   = mUser.city_name;
        if(mUser.first_name.length()<1)
          mDatingLayout.mInfoView.name   = "Без имени"; // РЕСУРСЫ !!!!!!!
        else
          mDatingLayout.mInfoView.name   = mUser.first_name;
        mDatingLayout.mInfoView.status = mUser.status;
        mDatingLayout.mInfoView.online = mUser.online;
        /*
        Http.imageLoader(mUser.avatars_big[0],mDatingLayout.mFaceView);
        
        if(mUser.avatars_big.length>1)
          Http.imageLoader(mUser.avatars_big[1],iv1);
        else
          iv1.setImageResource(R.drawable.ic_launcher);
        
        if(mUser.avatars_big.length>2)
          Http.imageLoader(mUser.avatars_big[2],iv2);
        else
          iv2.setImageResource(R.drawable.ic_launcher);
        */
        
        int width  = getWidth();
        int height = getHeight();
        

        final Bitmap bmp = Http.bitmapLoader(mUser.avatars_big[0]);
        
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        
        boolean LEG = false;
        if(w >= h) LEG = true;
        float ratio;
        /*
        if(LEG)
          ratio = (float)height/h;
        else
          ratio = (float)width/w;
        */
        ratio = Math.max(((float) width) / w, ((float) height) / h);
        
        if(ratio==0) ratio=1;
        
        Matrix matrix = new Matrix();
        matrix.postScale(ratio,ratio);
        
        Bitmap scaledBitmap = Bitmap.createBitmap(bmp,0,0,w,h,matrix,true);
        
        w = scaledBitmap.getWidth();
        h = scaledBitmap.getHeight();
        
        final Bitmap clippedBitmap;
        if(LEG) {
          // у горизонтальной, вырезаем по центру
          int offset_x = (scaledBitmap.getWidth()-width)/2;
          clippedBitmap = Bitmap.createBitmap(scaledBitmap,offset_x,0,width,height,null,false);
        } else
          // у вертикальной режим с верху
          clippedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,width,height,null,false);
        
        
        
        mDatingLayout.mFaceView.post(new Runnable() {
          @Override
          public void run() {
            mDatingLayout.mFaceView.setImageBitmap(clippedBitmap);
          }
        });
        
            
       if(mUser.avatars_big.length > 1) {
         final Bitmap bmp1 = Http.bitmapLoader(mUser.avatars_big[1]);
         iv1.post(new Runnable() {
           @Override
           public void run() {
             iv1.setImageBitmap(bmp1);
           }
         });
       } else 
         iv1.post(new Runnable() {
           @Override
           public void run() {
             iv1.setImageResource(R.drawable.ic_launcher);
           }
         });
       
       if(mUser.avatars_big.length > 2) {
         final Bitmap bmp2 = Http.bitmapLoader(mUser.avatars_big[2]);
         iv2.post(new Runnable() {
           @Override
           public void run() {
             iv2.setImageBitmap(bmp2);
           }
         });
       } else 
         iv2.post(new Runnable() {
           @Override
           public void run() {
             iv2.setImageResource(R.drawable.ic_launcher);
           }
         });
         
       
       mDatingLayout.post(new Runnable() {
        @Override
        public void run() {
          mDatingLayout.mStarsView.setEnabled(true);
          mDatingLayout.mProgress.setVisibility(View.INVISIBLE);
          mDatingLayout.mInfoView.setVisibility(View.VISIBLE);
          mDatingLayout.mFaceView.setVisibility(View.VISIBLE);
          mDatingLayout.invalidate();
        }
      });


       
      Looper.loop();
      }
    });
  }
  //---------------------------------------------------------------------------
  public void onProfileBtnClick() {
    mEventListener.openProfileActivity(mUser.uid);
  }
  //---------------------------------------------------------------------------
  public void onChatBtnClick() {
    mEventListener.openChatActivity(mUser.uid);
  }
  //---------------------------------------------------------------------------
  public void onRate(int index) {

        mDatingLayout.mStarsView.setEnabled(false);
        mDatingLayout.mInfoView.setVisibility(View.INVISIBLE);
        mDatingLayout.mFaceView.setVisibility(View.INVISIBLE);
        mDatingLayout.mProgress.setVisibility(View.VISIBLE);


        mEventListener.onRate(mUser.uid,index);
    //notifyDataChanged();
  }
  //---------------------------------------------------------------------------
  public void notifyDataChanged0() {
        mUser = mAdapter.getUser();
        
        mDatingLayout.mInfoView.age    = mUser.age;
        mDatingLayout.mResView.power  = Data.s_Power;
        mDatingLayout.mResView.money  = Data.s_Money;
        mDatingLayout.mInfoView.city   = mUser.city_name;
        if(mUser.first_name.length()<1)
          mDatingLayout.mInfoView.name   = "Без имени"; // РЕСУРСЫ !!!!!!!
        else
          mDatingLayout.mInfoView.name   = mUser.first_name;
        mDatingLayout.mInfoView.status = mUser.status;
        mDatingLayout.mInfoView.online = mUser.online;
        /*
        Http.imageLoader(mUser.avatars_big[0],mDatingLayout.mFaceView);
        
        if(mUser.avatars_big.length>1)
          Http.imageLoader(mUser.avatars_big[1],iv1);
        else
          iv1.setImageResource(R.drawable.ic_launcher);
        
        if(mUser.avatars_big.length>2)
          Http.imageLoader(mUser.avatars_big[2],iv2);
        else
          iv2.setImageResource(R.drawable.ic_launcher);
        */
        
        int width  = getWidth();
        int height = getHeight();
        

        final Bitmap bmp = Http.bitmapLoader(mUser.avatars_big[0]);
        
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        
        boolean LEG = false;
        if(w >= h) LEG = true;
        float ratio;
        /*
        if(LEG)
          ratio = (float)height/h;
        else
          ratio = (float)width/w;
        */
        ratio = Math.max(((float) width) / w, ((float) height) / h);
        
        if(ratio==0) ratio=1;
        
        Matrix matrix = new Matrix();
        matrix.postScale(ratio,ratio);
        
        Bitmap scaledBitmap = Bitmap.createBitmap(bmp,0,0,w,h,matrix,true);
        
        w = scaledBitmap.getWidth();
        h = scaledBitmap.getHeight();
        
        final Bitmap clippedBitmap;
        if(LEG) {
          // у горизонтальной, вырезаем по центру
          int offset_x = (scaledBitmap.getWidth()-width)/2;
          clippedBitmap = Bitmap.createBitmap(scaledBitmap,offset_x,0,width,height,null,false);
        } else
          // у вертикальной режим с верху
          clippedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,width,height,null,false);
        
        
        
        mDatingLayout.mFaceView.post(new Runnable() {
          @Override
          public void run() {
            mDatingLayout.mFaceView.setImageBitmap(clippedBitmap);
          }
        });
        
            
       if(mUser.avatars_big.length > 1) {
         final Bitmap bmp1 = Http.bitmapLoader(mUser.avatars_big[1]);
         iv1.post(new Runnable() {
           @Override
           public void run() {
             iv1.setImageBitmap(bmp1);
           }
         });
       } else 
         iv1.post(new Runnable() {
           @Override
           public void run() {
             iv1.setImageResource(R.drawable.ic_launcher);
           }
         });
       
       if(mUser.avatars_big.length > 2) {
         final Bitmap bmp2 = Http.bitmapLoader(mUser.avatars_big[2]);
         iv2.post(new Runnable() {
           @Override
           public void run() {
             iv2.setImageBitmap(bmp2);
           }
         });
       } else 
         iv2.post(new Runnable() {
           @Override
           public void run() {
             iv2.setImageResource(R.drawable.ic_launcher);
           }
         });
         
       
       mDatingLayout.post(new Runnable() {
        @Override
        public void run() {
          mDatingLayout.mStarsView.setEnabled(true);
          mDatingLayout.mProgress.setVisibility(View.INVISIBLE);
          mDatingLayout.mInfoView.setVisibility(View.VISIBLE);
          mDatingLayout.mFaceView.setVisibility(View.VISIBLE);
          mDatingLayout.invalidate();
        }
      });
  }
  //---------------------------------------------------------------------------
}
