package com.sonetica.topface.ui.rates;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class StarView extends View {
  // Data
  public int mRate;
  private float x;
  private float y;
  public static Bitmap mStarYellow;
  private static final Paint paintNumber = new Paint();
  //---------------------------------------------------------------------------
  public StarView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    paintNumber.setColor(Color.WHITE);
    paintNumber.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
    paintNumber.setTypeface(Typeface.DEFAULT_BOLD);
    paintNumber.setAntiAlias(true);
    paintNumber.setTextAlign(Paint.Align.CENTER);
    
    mStarYellow = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_yellow);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    // центрирование цифры 
    x = (mStarYellow.getWidth()/2);
    y = (mStarYellow.getHeight()/2+paintNumber.getTextSize()/2);
    
    setMeasuredDimension(mStarYellow.getWidth(),mStarYellow.getHeight());
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    canvas.drawBitmap(mStarYellow,0,0,paintNumber);
    canvas.drawText(""+mRate,x,y,paintNumber);
  }
  //---------------------------------------------------------------------------  
}
