package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ThumbView extends ImageView {
  // Data
  public int mPercent;
  public int mAge;
  public String mName;
  public boolean mOnline;
  // Constants
  private static Bitmap mOnlineBmp;
  private static Bitmap mOfflineBmp;
  private static Bitmap mHeartBmp;
  private static Paint s_PaintState;
  private static Paint s_PaintLine;
  private static Paint s_PaintText;
  static {
    s_PaintState = new Paint();
    s_PaintState.setColor(Color.WHITE);

    s_PaintLine = new Paint();
    s_PaintLine.setColor(Color.BLACK);
    s_PaintLine.setAlpha(154);
    
    s_PaintText = new Paint();
    s_PaintText.setColor(Color.WHITE);
    s_PaintText.setAntiAlias(true);
    s_PaintText.setTextSize(20);
  }
  //---------------------------------------------------------------------------
  public ThumbView(Context context,AttributeSet attrs) {
    super(context,attrs);
    //setBackgroundColor(Color.MAGENTA);
    mHeartBmp   = BitmapFactory.decodeResource(getResources(),R.drawable.ic_heart);
    mOnlineBmp  = BitmapFactory.decodeResource(getResources(),R.drawable.im_dating_online);
    mOfflineBmp = BitmapFactory.decodeResource(getResources(),R.drawable.im_dating_offline);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    //super.onDraw(canvas);
    int width  = getMeasuredWidth();
    int height = getMeasuredHeight();
    Bitmap bitmap = ((BitmapDrawable)getDrawable()).getBitmap();
    canvas.drawBitmap(bitmap,0,0,null);
    Rect lineRect = new Rect(0,height-29,width,height);
    canvas.drawRect(lineRect,s_PaintLine);
    if(mPercent!=0) {
      canvas.drawBitmap(mHeartBmp,lineRect.left,lineRect.top+6,s_PaintState);
      canvas.drawText(mPercent+" %",lineRect.left+mHeartBmp.getWidth(),lineRect.top+s_PaintText.getTextSize(),s_PaintText);
    } else {
      canvas.drawText(mName+", "+mAge,lineRect.left+mHeartBmp.getWidth(),lineRect.top+s_PaintText.getTextSize(),s_PaintText);
      if(mOnline)
        canvas.drawBitmap(mOnlineBmp,lineRect.right-mOnlineBmp.getWidth(),lineRect.top+6,s_PaintState);
      else
        canvas.drawBitmap(mOfflineBmp,lineRect.right-mOnlineBmp.getWidth(),lineRect.top+6,s_PaintState);
    }
  }
  //---------------------------------------------------------------------------
}
