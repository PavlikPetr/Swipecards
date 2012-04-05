package com.topface.topface.ui.rates;

import com.topface.topface.R;
import com.topface.topface.ui.Recycle;
import android.content.Context;
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
  private static final Paint paintNumber = new Paint();
  //---------------------------------------------------------------------------
  public StarView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    paintNumber.setColor(Color.WHITE);
    paintNumber.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
    paintNumber.setTypeface(Typeface.DEFAULT_BOLD);
    paintNumber.setAntiAlias(true);
    paintNumber.setTextAlign(Paint.Align.CENTER);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    // центрирование цифры 
    x = (Recycle.s_StarYellow.getWidth()/2);
    y = (Recycle.s_StarYellow.getHeight()/2+paintNumber.getTextSize()/2);
    
    setMeasuredDimension(Recycle.s_StarYellow.getWidth(),Recycle.s_StarYellow.getHeight());
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    canvas.drawBitmap(Recycle.s_StarYellow,0,0,paintNumber);
    canvas.drawText(""+mRate,x,y,paintNumber);
  }
  //---------------------------------------------------------------------------  
}
