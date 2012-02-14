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

public class FaceView extends ImageView {
  // Data
  public  int age;
  public  String name="";
  public  String city="";
  public  String status="";
  public  boolean online;
  
  private int mOffset_y;
  private boolean mHide;
  // Bitmaps
  private Bitmap mOnlineBmp;
  private Bitmap mOfflineBmp;
  private Bitmap mShadowTopBmp;
  private Bitmap mShadowBottomBmp;
  // Constants
  private Paint paint     = new Paint();
  private Paint paintName  = new Paint();
  private Paint paintCity   = new Paint();
  private Paint paintStatus  = new Paint();
  //---------------------------------------------------------------------------
  public FaceView(Context context) {
    super(context);
    
    mOnlineBmp  = BitmapFactory.decodeResource(getResources(),R.drawable.im_online);
    mOfflineBmp   = BitmapFactory.decodeResource(getResources(),R.drawable.im_offline);
    mShadowTopBmp  = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_top);
    mShadowBottomBmp = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_bottom);
    
    // paint
    paint.setColor(Color.RED);
    paint.setDither(true);
    
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
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);

    mOffset_y = DatingActivity.mHeaderBar.getHeight();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    if(!mHide) {
      // shadows
      canvas.drawBitmap(mShadowTopBmp,0,mOffset_y,paint);
      canvas.drawBitmap(mShadowBottomBmp,0,getHeight()-mShadowBottomBmp.getHeight(),paint);
      
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
  public void hideInfo() {
    mHide = !mHide;    
  }
  //---------------------------------------------------------------------------
  public void release() {
    mOnlineBmp.recycle();
    mOfflineBmp.recycle();
    mShadowTopBmp.recycle();
    mShadowBottomBmp.recycle();
    paint     = null;
    paintName  = null;
    paintCity   = null;
    paintStatus  = null;
  }
  //---------------------------------------------------------------------------
}
