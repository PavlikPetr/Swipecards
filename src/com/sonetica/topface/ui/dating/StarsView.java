package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class StarsView extends View implements View.OnTouchListener {
  // calss Star
  class Star {
    private int _coord_x;
    private int _coord_y;
    public  int _index;
    public Rect _rect;
    public Star(int x,int y,int index) {
      _coord_x = x;
      _coord_y = y;
      _index   = index;
      _rect = new Rect(x,y,x+mStar0.getWidth(),y+mStar0.getHeight());
    }
    public void draw(Canvas canvas) {
      canvas.drawBitmap(mStar0,_coord_x,_coord_y,starPaint);
      canvas.drawText(""+_index,_coord_x+12,_coord_y+18,starPaint);
    }
  }
  // Data
  private static Bitmap mStar0;
  private static Bitmap mStar1;
  private static Bitmap mStar2;
  private Star[] mStars;           // статичный массив объектов для отрисовки звезд;
  private InformerView mInformer;  // обсервер текущего нажатия на экран
  // Constants
  private static final Paint starPaint = new Paint();
  //---------------------------------------------------------------------------
  public StarsView(Context context,InformerView informer) {
    super(context);
    mStar0 = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_dating_star_yellow);
    mStar1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_dating_star_blue);
    mStar2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_dating_star_grey);
    mStars = new Star[10];
    mInformer = informer;
    setOnTouchListener(this);
    setBackgroundColor(Color.TRANSPARENT);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    
    int star = 0;
    int x = (int)event.getX();
    int y = (int)event.getY();
    //----------------
    for(int i=0;i<mStars.length;i++)
      if(mStars[i]._rect.contains(x,y))
        star = mStars[i]._index;
    //----------------
    
    
    switch(event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mInformer.setData(0,y,star);
        break;
      case MotionEvent.ACTION_MOVE:
        mInformer.setData(0,y,star);
        break;
      case MotionEvent.ACTION_UP:
        mInformer.setData(-100,-100,star);
        ((DatingActivity)getContext()).doRate(star); // Опасная операция, требует рефакторинг !!!!
        break;
      default:
        mInformer.setData(-100,-100,star);
        break;
    }
    mInformer.invalidate();
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
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    setMeasuredDimension(mStar0.getWidth(),MeasureSpec.getSize(heightMeasureSpec));
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
   
    int x = 0;
    int y = 0;
    int c = mStar0.getHeight();
    int z = (getHeight()-c*10)/9+c; // расстояние между звездами
    
    mStars[0] = new Star(x,y,10); y+=z;
    mStars[1] = new Star(x,y,9);  y+=z;
    mStars[2] = new Star(x,y,8);  y+=z;
    mStars[3] = new Star(x,y,7);  y+=z;
    mStars[4] = new Star(x,y,6);  y+=z;
    mStars[5] = new Star(x,y,5);  y+=z;
    mStars[6] = new Star(x,y,4);  y+=z;
    mStars[7] = new Star(x,y,3);  y+=z;
    mStars[8] = new Star(x,y,2);  y+=z;
    mStars[9] = new Star(x,y,1);  y+=z;
  }
  //---------------------------------------------------------------------------
}
