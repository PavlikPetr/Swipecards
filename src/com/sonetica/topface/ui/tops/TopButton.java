package com.sonetica.topface.ui.tops;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TopButton extends ImageView {
  // Data
  public String  mPercent;
  private static Paint s_Paint;
  private static Paint s_PaintLine;
  private static Paint s_PaintText;
  static {
    s_Paint = new Paint();
    s_Paint.setColor(Color.BLACK);

    s_PaintLine = new Paint();
    s_PaintLine.setColor(Color.BLACK);
    s_PaintLine.setAlpha(150);
    
    s_PaintText = new Paint();
    s_PaintText.setColor(Color.WHITE);
    s_PaintText.setTextSize(20);
  }
  //---------------------------------------------------------------------------
  public TopButton(Context context,AttributeSet attrs) {
    super(context,attrs);
    //setBackgroundColor(Color.MAGENTA);
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
    if(mPercent!=null)
    canvas.drawText(mPercent+" %",lineRect.left,lineRect.top+s_PaintText.getTextSize(),s_PaintText);
  }
  //---------------------------------------------------------------------------
}
