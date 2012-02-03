package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ImageView;

class InfoView extends ImageView {
  // Data
  public  int money;
  public  int power;
  public  int age;
  public  String name;
  public  String city;
  public  String status;
  public  boolean online;
  // Bitmaps
  private static Bitmap mPowerBmp;
  private static Bitmap mMoneyBmp;
  private static Bitmap mOnlineBmp;
  private static Bitmap mOfflineBmp;
  private static Bitmap mShadowTopBmp;
  private static Bitmap mShadowBottomBmp;
  // Constants
  private static final Paint paint = new Paint();
  private static final Paint paintResources = new Paint();
  private static final Paint paintName = new Paint();
  private static final Paint paintCity = new Paint();
  private static final Paint paintStatus = new Paint();
  //----------------------------------
  public InfoView(Context context) {
    super(context);
    
    setBackgroundColor(Color.TRANSPARENT);
    
    paint.setColor(Color.RED);
    
    // money, power
    paintResources.setColor(Color.WHITE);
    paintResources.setTextSize(14);
    paintResources.setAntiAlias(true);
    paintResources.setTypeface(Typeface.DEFAULT_BOLD);
    paintResources.setTextAlign(Paint.Align.RIGHT);
    
    // city
    paintCity.setColor(Color.WHITE);
    paintCity.setTextSize(14);
    paintCity.setAntiAlias(true);
    
    // status
    paintStatus.setColor(Color.WHITE);
    paintStatus.setTextSize(14);
    paintStatus.setAntiAlias(true);
    paintStatus.setSubpixelText(true);
    
    // name age online
    paintName.setColor(Color.WHITE);
    paintName.setTextSize(18);
    paintName.setTypeface(Typeface.DEFAULT_BOLD);
    paintName.setAntiAlias(true);
    
    mPowerBmp        = BitmapFactory.decodeResource(getResources(),R.drawable.ic_dating_power);
    mMoneyBmp        = BitmapFactory.decodeResource(getResources(),R.drawable.ic_dating_money);
    mOnlineBmp       = BitmapFactory.decodeResource(getResources(),R.drawable.im_dating_online);
    mOfflineBmp      = BitmapFactory.decodeResource(getResources(),R.drawable.im_dating_offline);
    mShadowTopBmp    = BitmapFactory.decodeResource(getResources(),R.drawable.im_dating_shadow_top);
    mShadowBottomBmp = BitmapFactory.decodeResource(getResources(),R.drawable.im_dating_shadow_bottom);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int width  = MeasureSpec.getSize(widthMeasureSpec);

    setMeasuredDimension(width,height);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    super.onLayout(changed,left,top,right,bottom);
  }
  //----------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    // shadows
    canvas.drawBitmap(mShadowTopBmp,0,0,paint);
    canvas.drawBitmap(mShadowBottomBmp,0,getHeight()-mShadowBottomBmp.getHeight(),paint);
    
    // money, power
    float offset_x = 50;
    float offset_y = 8;
    canvas.drawText(""+power,offset_x,offset_y+(int)(mPowerBmp.getHeight()/1.4),paintResources);
    canvas.drawBitmap(mPowerBmp,offset_x,offset_y,paint);
    offset_x *= 2.5;
    canvas.drawText(""+money,offset_x,offset_y+(int)(mMoneyBmp.getHeight()/1.4),paintResources);
    canvas.drawBitmap(mMoneyBmp,offset_x+4,offset_y,paint);
    
    offset_x = 10;
    offset_y = getHeight()-paintCity.getTextSize();
    
    //city
    canvas.drawText(city,offset_x,offset_y,paintCity);
    
    //paintStatus.
    
    // status
    canvas.drawText(status,offset_x,(float)(offset_y-paintStatus.getTextSize()*1.5),paintStatus);
    
    offset_y-=60;
    
    // name age online
    String name_age = name+", "+age; 
    canvas.drawText(name_age,offset_x,offset_y,paintName);
    float offset_z = paintName.measureText(name_age)+4;
    if(online)
      canvas.drawBitmap(mOnlineBmp,offset_x+offset_z,offset_y-mOnlineBmp.getHeight(),paint);
    else
      canvas.drawBitmap(mOfflineBmp,offset_x+offset_z,offset_y-mOnlineBmp.getHeight(),paint);
    

  }
  //----------------------------------
}
