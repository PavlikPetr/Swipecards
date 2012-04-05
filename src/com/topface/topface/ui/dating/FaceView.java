package com.topface.topface.ui.dating;

import com.topface.topface.R;
import com.topface.topface.ui.Recycle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;

/*
 *   информация оцениваемого пользователя, ПЕРЕДЕЛАТЬ на layout и объекты view
 */
public class FaceView extends ImageView {
  // Data
  public  int age;
  public  String name = "";
  public  String city = "";
  public  String status = "";
  public  boolean online;
  
  private boolean mHide;
  // Constants
  private Paint paint     = new Paint();
  private Paint paintName  = new Paint();
  private Paint paintCity   = new Paint();
  private Paint paintStatus  = new Paint();
  //---------------------------------------------------------------------------
  public FaceView(Context context) {
    super(context);

    //mShadowTopBmp  = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_top);
    //mShadowBottomBmp = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_bottom);
    
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
    paintName.setAntiAlias(true);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    if(getVisibility()==View.VISIBLE) {
      // shadows
      //canvas.drawBitmap(mShadowTopBmp,0,0,paint);
      //canvas.drawBitmap(mShadowBottomBmp,0,getHeight()-mShadowBottomBmp.getHeight(),paint);
      
      //city
      float offset_x = getResources().getDimension(R.dimen.city_offset_x_size);
      float offset_y = (float)(getHeight()-paintCity.getTextSize()*1.5);
      canvas.drawText(city,offset_x,offset_y,paintCity);
  
      // name age online
      offset_y-=getResources().getDimension(R.dimen.name_offset_y_size);
      String name_age = name+", "+age; 
      canvas.drawText(name_age,offset_x,offset_y,paintName);
      float offset_z = paintName.measureText(name_age)+Recycle.s_Online.getWidth()/2;
      if(online)
        canvas.drawBitmap(Recycle.s_Online,offset_x+offset_z,offset_y-Recycle.s_Online.getHeight(),paint);
      else
        canvas.drawBitmap(Recycle.s_Offline,offset_x+offset_z,offset_y-Recycle.s_Offline.getHeight(),paint);
      
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
    int numChar  = paint.breakText(status,false,170,null); // 170 магическое число
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
    paint     = null;
    paintName  = null;
    paintCity   = null;
    paintStatus  = null;
  }
  //---------------------------------------------------------------------------
}
