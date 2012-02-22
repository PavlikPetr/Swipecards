package com.sonetica.topface.ui.dating_ex;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PointView extends View {
  // Data
  public int mCountPoints;
  public int mCurrentPoint;
  private int width;
  private int height;
  private int offset_x;
  private int offset_y;
  private Bitmap mStar;
  private Bitmap mStarOff;
  private Bitmap mPoint;
  private Bitmap mPointOff;
  private static Paint paint = new Paint(); 
  //---------------------------------------------------------------------------
  public PointView(Context context,AttributeSet attrs) {
    super(context,attrs);
    mStar  = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star);
    mStarOff  = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_off);
    mPoint = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_point);
    mPointOff = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_point_off);
    setBackgroundColor(Color.TRANSPARENT);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),mStar.getHeight()*2);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    width  = getMeasuredWidth();
    height = getMeasuredHeight();
    offset_x = (width-mStar.getWidth()*mCountPoints)/2;
    offset_y = (height-mStar.getHeight())/2;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int x=offset_x;
    canvas.drawBitmap(mCurrentPoint==0?mStar:mStarOff,x,offset_y,paint);
    for(int i=0;i<mCountPoints-1;i++) {
      x+=mStar.getWidth()*1.5;
      canvas.drawBitmap(i==mCurrentPoint-1?mPoint:mPointOff,x,offset_y,paint);
    }

  }
  //---------------------------------------------------------------------------
}
