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
import android.view.View;
import android.view.View.MeasureSpec;

public class ResourcesView extends View {
  // Data
  public  int money;
  public  int power;
  // Constants
  private static Bitmap mPowerBmp;
  private static Bitmap mMoneyBmp;
  private static final Paint paintResources = new Paint();
  private static final Paint paint = new Paint();
  //---------------------------------------------------------------------------
  public ResourcesView(Context context) {
    super(context);
    
    // money, power
    paintResources.setColor(Color.WHITE);
    paintResources.setTextSize(getResources().getDimension(R.dimen.resources_font_size));
    paintResources.setAntiAlias(true);
    paintResources.setTypeface(Typeface.DEFAULT_BOLD);
    paintResources.setTextAlign(Paint.Align.RIGHT);
    
    mPowerBmp        = BitmapFactory.decodeResource(getResources(),R.drawable.dating_power);
    mMoneyBmp        = BitmapFactory.decodeResource(getResources(),R.drawable.dating_money);
    
    money = Data.s_Money;
    power = Data.s_Power;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int width  = MeasureSpec.getSize(widthMeasureSpec);
    
    float offset_x = getResources().getDimension(R.dimen.resources_offset_x_size);
    offset_x *= 2.3;
    offset_x += mMoneyBmp.getWidth() * 2;
    
    float offset_y = getResources().getDimension(R.dimen.resources_offset_y_size);
    offset_y += mMoneyBmp.getHeight();

    setMeasuredDimension((int)offset_x,(int)offset_y);
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
    
    // money, power
    float offset_x = getResources().getDimension(R.dimen.resources_offset_x_size);
    float offset_y = getResources().getDimension(R.dimen.resources_offset_y_size);
    canvas.drawText(""+power,offset_x,offset_y+(int)(mPowerBmp.getHeight()/1.4),paintResources);
    canvas.drawBitmap(mPowerBmp,offset_x,offset_y,paint);
    offset_x *= 2.3;
    canvas.drawText(""+money,offset_x,offset_y+(int)(mMoneyBmp.getHeight()/1.4),paintResources);
    canvas.drawBitmap(mMoneyBmp,offset_x+6,offset_y,paint);
  }
  //---------------------------------------------------------------------------
}
