package com.sonetica.topface.ui.dating;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ImageView;

class InfoView_ extends ImageView {
  // Data
  //public  int money;
  //public  int power;
  public  int age;
  public  String name;
  public  String city;
  public  String status;
  public  boolean online;
  // Bitmaps
//  private static Bitmap mPowerBmp;
//  private static Bitmap mMoneyBmp;
  private static Bitmap mOnlineBmp;
  private static Bitmap mOfflineBmp;
  private static Bitmap mShadowTopBmp;
  private static Bitmap mShadowBottomBmp;
  // Constants
  private static final Paint paint      = new Paint();
  private static final Paint paintName   = new Paint();
  private static final Paint paintCity    = new Paint();
  private static final Paint paintStatus   = new Paint();
  //private static final Paint paintResources = new Paint();
  //----------------------------------
  public InfoView_(Context context) {
    super(context);
    
    setBackgroundColor(Color.TRANSPARENT);
    
    paint.setColor(Color.RED);
    paint.setDither(true);
    
      // money, power
  //    paintResources.setColor(Color.WHITE);
  //    paintResources.setTextSize(getResources().getDimension(R.dimen.resources_font_size));
  //    paintResources.setAntiAlias(true);
  //    paintResources.setTypeface(Typeface.DEFAULT_BOLD);
  //    paintResources.setTextAlign(Paint.Align.RIGHT);
    
    // city
    paintCity.setColor(Color.WHITE);
    paintCity.setTextSize(getResources().getDimension(R.dimen.city_font_size));
    paintCity.setAntiAlias(true);
    
    // status
    paintStatus.setColor(Color.WHITE);
    paintStatus.setTextSize(getResources().getDimension(R.dimen.status_font_size));
    paintStatus.setAntiAlias(true);
    paintStatus.setSubpixelText(true);
    
    // name age online
    paintName.setColor(Color.WHITE);
    paintName.setTextSize(getResources().getDimension(R.dimen.name_font_size));
    paintName.setTypeface(Typeface.DEFAULT_BOLD);
    paintName.setAntiAlias(true);

    mOnlineBmp       = BitmapFactory.decodeResource(getResources(),R.drawable.im_online);
    mOfflineBmp      = BitmapFactory.decodeResource(getResources(),R.drawable.im_offline);
    //mPowerBmp        = BitmapFactory.decodeResource(getResources(),R.drawable.dating_power);
    //mMoneyBmp        = BitmapFactory.decodeResource(getResources(),R.drawable.dating_money);
    mShadowTopBmp    = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_top);
    mShadowBottomBmp = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_bottom);
    
    //money = Data.s_Money;
    //power = Data.s_Power;
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
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    // shadows
    canvas.drawBitmap(mShadowTopBmp,0,0,paint);
    canvas.drawBitmap(mShadowBottomBmp,0,getHeight()-mShadowBottomBmp.getHeight(),paint);
    
//    // money, power
//    float offset_x = getResources().getDimension(R.dimen.resources_offset_x_size);
//    float offset_y = getResources().getDimension(R.dimen.resources_offset_y_size);
//    canvas.drawText(""+power,offset_x,offset_y+(int)(mPowerBmp.getHeight()/1.4),paintResources);
//    canvas.drawBitmap(mPowerBmp,offset_x,offset_y,paint);
//    offset_x *= 2.3;
//    canvas.drawText(""+money,offset_x,offset_y+(int)(mMoneyBmp.getHeight()/1.4),paintResources);
//    canvas.drawBitmap(mMoneyBmp,offset_x+6,offset_y,paint);
    
    //city
    float offset_x = getResources().getDimension(R.dimen.city_offset_x_size);
    float offset_y = getHeight()-paintCity.getTextSize()/2;
    canvas.drawText(city,offset_x,offset_y,paintCity);

    // name age online
    offset_y-=getResources().getDimension(R.dimen.name_offset_y_size);
    String name_age = name+", "+age; 
    canvas.drawText(name_age,offset_x,offset_y,paintName);
    float offset_z = paintName.measureText(name_age)+mOnlineBmp.getWidth()/2;
    if(online)
      canvas.drawBitmap(mOnlineBmp,offset_x+offset_z,offset_y-mOnlineBmp.getHeight(),paint);
    else
      canvas.drawBitmap(mOfflineBmp,offset_x+offset_z,offset_y-mOnlineBmp.getHeight(),paint);
    
    // status
    drawStatus(canvas,status,offset_x,offset_y);

  }
  //---------------------------------------------------------------------------
  public void drawStatus(Canvas canvas,String text,float offset_x,float offset_y) {
    float k = 1.6f;
    
    if(status.length()<=1)
      return;
    
    int count    = 0;
    int size     = text.length();   // длина целой строки
    int numChar  = paint.breakText(status,true,140,null); // кол-во влезающих символов; БЛЕАТЬ, что за 140 !!!!
    int numStart = 0;               // начало обреза
    int numEnd   = numChar;         // конец обреза
    
    offset_y += paintName.getTextSize()/1.4;
    
    do {
      count++;  // чоп чоп
      
      canvas.drawText(status.substring(numStart,numEnd),offset_x,offset_y,paintStatus);
      
      offset_y += paintName.getTextSize() / k;
      
      if(size-numEnd<numChar || count==5) {
        if(size-numEnd > numChar)
          canvas.drawText(status.substring(numEnd,numEnd+numChar)+"...",offset_x,offset_y,paintStatus);
        else
          canvas.drawText(status.substring(numEnd,size),offset_x,offset_y,paintStatus);
        break;
      }
      
      numStart = numEnd;
      numEnd  += numChar;
      
    } while(true);
  }
  //---------------------------------------------------------------------------
}
