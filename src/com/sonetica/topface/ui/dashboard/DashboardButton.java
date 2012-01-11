package com.sonetica.topface.ui.dashboard;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/*
 *  Класс для кнопок на Dashboard активити (НЕ ИСПОЛЬЗУЕТСЯ)
 *  для отображения поверх кнопки иконки кол-во сообщений, оценок и т.д.
 */
public class DashboardButton extends Button {
  // Data
  public int mNotify;
  private static Paint mPaint;
  private Bitmap mRedInformer;
  static {
    mPaint = new Paint();
    mPaint.setTextSize(18);
    mPaint.setTypeface(Typeface.DEFAULT_BOLD);
  }
  //---------------------------------------------------------------------------
  public DashboardButton(Context context,AttributeSet attrs) {
    super(context,attrs);
    mNotify = -1;
    mRedInformer = BitmapFactory.decodeResource(getResources(),R.drawable.im_red_informer);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if(mNotify>0) {
      canvas.drawBitmap(mRedInformer,5,5,mPaint);
      canvas.drawText(String.valueOf(mNotify),30,30,mPaint);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
  }
  //--------------------------------------------------------------------------- 
}
