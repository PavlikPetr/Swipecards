package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
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
    public  int _index;
    private int _color;
    private int _coord_x;
    private int _coord_y;
    private int _bmp_y;
    private int _widht;
    private int _height;
    public Rect _rect;
    // Ctor
    public Star(int x,int y,int width,int height,int index,int color) {
      _coord_x = x;
      _coord_y = y;
      _bmp_y = y + ((height - mStar0.getHeight()) / 2);   // центрирование по вертикале
      _widht   = width;
      _height  = height;
      _index   = index;
      _color   = color;  
      _rect = new Rect(_coord_x,_coord_y,_coord_x+_widht,_coord_y+_height);
    }
    public void draw(Canvas canvas) {
      starPaint.setColor(_color);
      //canvas.drawRect(_rect,starPaint);
      canvas.drawBitmap(mStar0,_coord_x,_bmp_y,starPaint);
      canvas.drawText(""+_index,_coord_x+12,_coord_y+18,starPaint);
    }
  }
  // Data
  private static Bitmap mStar0;
  //private static Bitmap mStar1;
  //private static Bitmap mStar2;
  private float[] lastYs;
  private Star[] mStars;           // статичный массив объектов для отрисовки звезд;
  private InformerView mInformer;  // обсервер текущего нажатия на экран
  // Constants
  private static final int EVENT_COUNT = 3;   // число последних запоминаемых координат пальца
  private static final int STARS_COUNT = 10;  // кол-во звезд на фрейме
  private static final Paint starPaint = new Paint();
  //---------------------------------------------------------------------------
  public StarsView(Context context,InformerView informer) {
    super(context);
    mStars = new Star[STARS_COUNT];
    lastYs = new float[EVENT_COUNT];
    mInformer = informer;
    
    mStar0 = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_dating_star_yellow);
    //mStar1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_dating_star_blue);
    //mStar2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_dating_star_grey);
    
    setOnTouchListener(this);
    setBackgroundColor(Color.TRANSPARENT);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    // текущие координаты пальца
    int x = (int)event.getX();
    int y = (int)event.getY();

    // определяем на какой звезде находится палец
    int star_index = 0;
    for(int i=0;i<mStars.length;i++)
      if(mStars[i]._rect.contains(x,y))
        star_index = mStars[i]._index;
    
    switch(event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        for(int i = 0; i < EVENT_COUNT; i++)
          lastYs[i] = y;
        break;
      case MotionEvent.ACTION_MOVE:
        for (int i = 0; i < EVENT_COUNT-1; i++)
          lastYs[i] = lastYs[i+1];
        lastYs[EVENT_COUNT-1] = y;
        break;
      case MotionEvent.ACTION_UP:
        for(int i = 0; i < EVENT_COUNT; i++)
          lastYs[i] = -100;
        ((DatingActivity)getContext()).doRate(0,star_index); // Опасная операция, требует рефакторинг !!!!
        break;
      default:
        for(int i = 0; i < EVENT_COUNT; i++)
          lastYs[i] = -100;
        break;
    }
    
    float averageY = 0.0f;
    for(int i = 0; i < EVENT_COUNT; i++)
      averageY += lastYs[i];
    averageY /= EVENT_COUNT;
    
    mInformer.setData(averageY,star_index);
    mInformer.invalidate();

    Debug.log("NULL"," >>>> " + averageY);
    
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
    int parent_h = MeasureSpec.getSize(heightMeasureSpec); // вычисляем предоставленную нам высоту для отрисовки
    int width = (int)(mStar0.getWidth() * 1.3);            // вычисление своей ширины, вынести в константы !!!

    setMeasuredDimension(width,parent_h);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    // размеры одной звезды
    int star_w = getMeasuredWidth();
    int star_h = getMeasuredHeight() / STARS_COUNT; // все доступное расстояние делим между звездами
    
    // координаты для позиционирования звезд
    int x = 0;
    int y = 0;
    
    mStars[0] = new Star(x,y,star_w,star_h,10,Color.WHITE);  y+=star_h; // смещение для отрисовки след звезды
    mStars[1] = new Star(x,y,star_w,star_h,9,Color.CYAN);    y+=star_h;
    mStars[2] = new Star(x,y,star_w,star_h,8,Color.RED);     y+=star_h;
    mStars[3] = new Star(x,y,star_w,star_h,7,Color.BLUE);    y+=star_h;
    mStars[4] = new Star(x,y,star_w,star_h,6,Color.GRAY);    y+=star_h;
    mStars[5] = new Star(x,y,star_w,star_h,5,Color.YELLOW);  y+=star_h;
    mStars[6] = new Star(x,y,star_w,star_h,4,Color.CYAN);    y+=star_h;
    mStars[7] = new Star(x,y,star_w,star_h,3,Color.DKGRAY);  y+=star_h;
    mStars[8] = new Star(x,y,star_w,star_h,2,Color.GREEN);   y+=star_h;
    mStars[9] = new Star(x,y,star_w,star_h,1,Color.MAGENTA);
  }
  //---------------------------------------------------------------------------
}
