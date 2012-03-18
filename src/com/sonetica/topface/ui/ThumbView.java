package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ThumbView extends ImageView {
  // Data
  public int     mPercent;
  public int     mAge;
  public String  mName;
  public boolean mOnline;
  public int     mCity;
  // Constants
  //private static Bitmap mPeople;
  public static Bitmap mOnlineBmp;
  public static Bitmap mOfflineBmp;
  public static Bitmap mHeartBmp;
  public static Paint s_PaintState;
  public static Paint s_PaintLine;
  public static Paint s_PaintText;

  //---------------------------------------------------------------------------
  public ThumbView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    setBackgroundColor(Color.TRANSPARENT);
    setScaleType(ScaleType.CENTER);
    
    //mPeople     = BitmapFactory.decodeResource(getResources(),R.drawable.icon_people);
    if(mHeartBmp == null)
      mHeartBmp = BitmapFactory.decodeResource(getResources(),R.drawable.tops_heart);
    if(mOnlineBmp == null)
      mOnlineBmp = BitmapFactory.decodeResource(getResources(),R.drawable.im_online);
    if(mOfflineBmp == null)
      mOfflineBmp = BitmapFactory.decodeResource(getResources(),R.drawable.im_offline);
    if(s_PaintState == null) {
      s_PaintState = new Paint();
      s_PaintState.setColor(Color.WHITE);
    }
    if(s_PaintLine == null) {
      s_PaintLine = new Paint();
      s_PaintLine.setColor(Color.BLACK);
      s_PaintLine.setAlpha(154);
      
    }
    if(s_PaintText == null) {
      s_PaintText = new Paint();
      s_PaintText.setColor(Color.WHITE);
      s_PaintText.setAntiAlias(true);
      s_PaintText.setTextSize(16);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    int width  = getMeasuredWidth();
    int height = getMeasuredHeight();
    
    // People
    //canvas.drawBitmap(mPeople,(width-mPeople.getWidth())/2,(height-mPeople.getHeight())/2,s_PaintState);
    
    //Bitmap bitmap = ((BitmapDrawable)getDrawable()).getBitmap();
    //canvas.drawBitmap(bitmap,0,0,null);
    Rect lineRect = new Rect(0,height-32,width,height);
    canvas.drawRect(lineRect,s_PaintLine);
    
    // city
    //canvas.drawText(mCity+"",10,20,s_PaintText);
    
    if(mPercent!=0) {
        float x = lineRect.left+mHeartBmp.getWidth()/2;
        float y = (lineRect.height()-mHeartBmp.getHeight())/2;
      // heart
      canvas.drawBitmap(mHeartBmp,x,lineRect.top+y,s_PaintState);
        x = x*2 + mHeartBmp.getWidth();
      canvas.drawText(mPercent+" %",x,(float)(lineRect.bottom-s_PaintText.getTextSize()/1.5),s_PaintText);
    } else {
      float x = (float)(lineRect.right-mOnlineBmp.getWidth()*1.25);
      float y = (lineRect.height()-mOnlineBmp.getHeight())/2; 
      // name
      canvas.drawText(mName+", "+mAge,lineRect.left+mHeartBmp.getWidth()/2,(float)(lineRect.bottom-s_PaintText.getTextSize()/1.5),s_PaintText);
      // online
      if(mOnline)
        canvas.drawBitmap(mOnlineBmp,x,lineRect.top+y,s_PaintState);
      else
        canvas.drawBitmap(mOfflineBmp,x,lineRect.top+y,s_PaintState);
    }
  }
  //---------------------------------------------------------------------------
}
