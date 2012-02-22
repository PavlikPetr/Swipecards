package com.sonetica.topface.ui.dating_ex;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class DatingLayout extends ViewGroup implements View.OnClickListener {
  // Data
  private FaceView mFaceView;
  private RateControl mRateControl;
  private ProgressBar mProgress;
  private ResourcesView mResView;
  //---------------------------------------------------------------------------
  public DatingLayout(Context context) {
    this(context,null);
  }
  //---------------------------------------------------------------------------
  public DatingLayout(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    setId(R.id.datingLayout);
    setOnClickListener(this);
    setBackgroundColor(Color.TRANSPARENT);
    
    mProgress = new ProgressBar(context);
    addView(mProgress);
    
    mFaceView = new FaceView(context);
    mFaceView.setVisibility(View.INVISIBLE);
    addView(mFaceView);
    
    mResView = new ResourcesView(context);
    addView(mResView);
    
    mRateControl = new RateControl(context);
    addView(mRateControl);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);

    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(widthMeasureSpec,heightMeasureSpec);
    
    mProgress.measure(0,0);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    mFaceView.layout(left,top,right,bottom);
    
    int offset_y = DatingActivity.mHeaderBar.getHeight();
    
    mResView.layout(0,offset_y,mResView.getMeasuredWidth(),offset_y+mResView.getMeasuredHeight());
    mRateControl.layout(0,offset_y,mRateControl.getMeasuredWidth(),offset_y+mRateControl.getMeasuredHeight());
    
    int stars_x = (getMeasuredWidth() - mProgress.getMeasuredWidth())/2;
    int stars_y = (getMeasuredHeight() - mProgress.getMeasuredHeight())/2;
    mProgress.layout(stars_x,stars_y,stars_x+mProgress.getMeasuredWidth(),stars_y+mProgress.getMeasuredHeight());
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View v) {
    int visibility = mRateControl.getVisibility()==VISIBLE?INVISIBLE:VISIBLE;
    mRateControl.setVisibility(visibility);
    
    visibility = DatingActivity.mHeaderBar.getVisibility()==VISIBLE?INVISIBLE:VISIBLE;
    DatingActivity.mHeaderBar.setVisibility(visibility);
    
    visibility = mResView.getVisibility()==VISIBLE?INVISIBLE:VISIBLE;
    mResView.setVisibility(visibility);
    
    mFaceView.hideInfo();
  }
  //---------------------------------------------------------------------------
  public void progress(int visibility) {
    if(visibility == View.VISIBLE) { // если включен прогресс
      mRateControl.setBlock(false);  // выключаем реакцию на контролах
      this.setEnabled(false);
    } else {                         // и наооборот
      mRateControl.setBlock(true);
      this.setEnabled(true);      
    }
    //mProgress.setVisibility(mProgress.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
    mProgress.setVisibility(visibility);
  }
  //---------------------------------------------------------------------------
  public void setImageResource(int resId) {
    mFaceView.setImageResource(resId);
  }
  //---------------------------------------------------------------------------
  public void setImageData(Bitmap bitmap,SearchUser user) {
    mResView.money = Data.s_Money;
    mResView.power = Data.s_Power;
    
    mFaceView.age    = user.age;
    mFaceView.city   = user.city_name;
    mFaceView.name   = user.first_name;
    mFaceView.online = user.online;
    mFaceView.status = user.status;
    
    mFaceView.setImageBitmap(bitmap);
  }
  //---------------------------------------------------------------------------
  public void setFaceVisibility(int visibility) {
    mFaceView.setVisibility(visibility);
  }
  //---------------------------------------------------------------------------
  public void release() {
    mFaceView.release();
    mFaceView = null;
    mRateControl.release();
    mRateControl = null;
    mResView.release();
    mResView = null;
    
    mProgress = null;
  }
  //---------------------------------------------------------------------------
}
