package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TestScroller extends ImageView implements View.OnTouchListener {
  // Data
  private Bitmap mBitmap1;
  private Bitmap mBitmap2;
  private Canvas mCanvas1;
  private Canvas mCanvas2;
  private Rect mSrcRect0;
  private Rect mDstRect1;
  private Rect mDstRect2;
  //---------------------------------------------------------------------------
  public TestScroller(Context context,AttributeSet attrs) {
    super(context,attrs);

    setBackgroundColor(Color.WHITE);
    setOnTouchListener(this);
  }
  //---------------------------------------------------------------------------
  @Override
  protected boolean setFrame(int l,int t,int r,int b) {
    if(mBitmap1==null || mBitmap2==null) {
      mBitmap1 = Bitmap.createBitmap(r-l,b-t,Config.RGB_565);
      mBitmap2 = Bitmap.createBitmap(r-l,b-t,Config.RGB_565);
      mCanvas1 = new Canvas(mBitmap1);
      mCanvas2 = new Canvas(mBitmap2);
      mCanvas1.drawRGB(255,0,0);
      mCanvas2.drawRGB(0,255,0);
      mSrcRect0 = new Rect(0,0,r-l,b-t);
      mDstRect1 = new Rect(0,0,r-l,b-t);
      mDstRect2 = new Rect(r-l,0,(r-l)+(r-l),b-t);
    }

    return super.setFrame(l,t,r,b);
  }
  //---------------------------------------------------------------------------
  Paint paint = new Paint();
  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawBitmap(mBitmap1,mSrcRect0,mDstRect1,paint);
    canvas.drawBitmap(mBitmap2,mSrcRect0,mDstRect2,paint);
  }
  //---------------------------------------------------------------------------
  private void process() {
    
  }
  //---------------------------------------------------------------------------
  boolean mIsDrag;
  int downX;
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    int action = event.getAction();
    switch(action) {
      case MotionEvent.ACTION_DOWN:
        mIsDrag = true;
        downX = (int)event.getX();
        break;
      case MotionEvent.ACTION_MOVE:
        if(mIsDrag) {
          mDstRect1.offset(downX-(int)event.getX(),0);
          mDstRect2.offset(downX-(int)event.getX(),0);
          invalidate();
        }
        break;
      case MotionEvent.ACTION_UP:
        mIsDrag = false;
        break;
      default:
        break;
    }
    return true;
  }
  //---------------------------------------------------------------------------
}

