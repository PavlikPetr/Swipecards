package com.sonetica.topface.ui.dating;

import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.Toast;

public class StarsView extends View implements View.OnTouchListener {
  // calss Star
  class Star {
    private Rect mRect;
    private int  mColor;
    private int  _x;
    private int  _y;
    private int  _width  = mWidth;
    private int  _height = mWidth;
    public Star(int x,int y,int color) {
      _x = x;
      _y = y;
      mColor = color;
    }
    public void draw(Canvas canvas) {
      starPaint.setColor(mColor);
      canvas.drawRect(_x,_y,_x+_width,_y+_height,starPaint);
    }
  }
  // Data
  private int mWidth = 50;
  private Star[] mStars;
  private InformerView mInformer;
  // Constants
  private static final Paint starPaint = new Paint();
  //---------------------------------------------------------------------------
  public StarsView(Context context,InformerView informer) {
    super(context);
    mStars = new Star[10];
    mInformer = informer;
    setOnTouchListener(this);
    setBackgroundColor(Color.WHITE);
  }
  //---------------------------------------------------------------------------
  public int getWidthEx() {
    return mWidth;
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    switch(event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        //Toast.makeText(getContext(),"down",Toast.LENGTH_SHORT).show();
        break;
      case MotionEvent.ACTION_MOVE:
        mInformer.setPosition(0,(int)event.getY());
        mInformer.invalidate();
        break;
      case MotionEvent.ACTION_UP:
        break;
      default:
        break;
    }
    invalidate();
    return true;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for(Star item : mStars)
      item.draw(canvas);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    //super.onLayout(changed,left,top,right,bottom);
   
    int x = 0;
    int y = 0;
    mStars[0] = new Star(x,y,Color.BLUE);y+=50;
    mStars[1] = new Star(x,y,Color.BLACK);y+=50;
    mStars[2] = new Star(x,y,Color.CYAN);y+=50;
    mStars[3] = new Star(x,y,Color.DKGRAY);y+=50;
    mStars[4] = new Star(x,y,Color.GRAY);y+=50;
    mStars[5] = new Star(x,y,Color.GREEN);y+=50;
    mStars[6] = new Star(x,y,Color.LTGRAY);y+=50;
    mStars[7] = new Star(x,y,Color.MAGENTA);y+=50;
    mStars[8] = new Star(x,y,Color.WHITE);y+=50;
    mStars[9] = new Star(x,y,Color.YELLOW);y+=50;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
//    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
//    int width0  = MeasureSpec.getSize(widthMeasureSpec);
//    int height0 = MeasureSpec.getSize(heightMeasureSpec);
//    Debug.log(this,">SW onMeasure, w:"+width0+" h:"+height0);
  }
  //---------------------------------------------------------------------------
}
