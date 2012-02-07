package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

public class StarsView extends View implements View.OnTouchListener {
  //---------------------------------------------------------------------------
  // calss Star
  //---------------------------------------------------------------------------
  static class Star {
    public  int _index;    // оценка
    private int _x;
    private int _y;
    private int _bmp_y;
    private int _widht;
    private int _height;
    public Rect _rect;      // 
    public boolean pressed; // состояние
    public static int average=6;     // средняя выставляемвя оценка 
    // Ctor
    public Star(int x,int y,int width,int height,int index) {
      _x = x;
      _y = y;
      _bmp_y = y + ((height - mStarYellow.getHeight()) / 2);   // центрирование по вертикале
      _widht   = width;
      _height  = height;
      _index   = index;
      _rect = new Rect(_x,_y,_x+_widht,_y+_height);
    }
    public void draw(Canvas canvas) {
      if(_index==average)
        if(!pressed)
          canvas.drawBitmap(mStarBlue,_x,_bmp_y,paintStar);
        else
          canvas.drawBitmap(mStarBlueActive,_x,_bmp_y,paintStar);
      
      if(_index>average)
        if(!pressed)
          canvas.drawBitmap(mStarYellow,_x,_bmp_y,paintStar);
        else
          canvas.drawBitmap(mStarYellowActive,_x,_bmp_y,paintStar);
      
      if(_index<average)
        if(!pressed)
          canvas.drawBitmap(mStarGrey,_x,_bmp_y,paintStar);
        else
          canvas.drawBitmap(mStarGreyActive,_x,_bmp_y,paintStar);
      
      // Star Index         вынести расчеты !!!!!!!!
      canvas.drawText(""+_index,(float)(_x+mStarYellow.getWidth()/2),(float)(_bmp_y+mStarYellow.getHeight()/1.6),paintNumber);
    }
  }
  //---------------------------------------------------------------------------
  // Data
  private static Bitmap mStarYellow;
  private static Bitmap mStarYellowActive;
  private static Bitmap mStarBlue;
  private static Bitmap mStarBlueActive;
  private static Bitmap mStarGrey;
  private static Bitmap mStarGreyActive;
  private float[] mLastYs;              // массив последних нажатий
  private Star[]  mStars;              // статичный массив объектов для отрисовки звезд;
  private InformerView mInformerView;  // обсервер текущего нажатия на экран
  //private TextView mPopupView;
  private float   mFontSize;
  // Constants
  private static final int EVENT_COUNT = 3;   // число последних запоминаемых координат пальца
  private static final int STARS_COUNT = 10;  // кол-во звезд на фрейме
  private static final Paint paintStar   = new Paint();
  private static final Paint paintNumber = new Paint();
  //---------------------------------------------------------------------------
  public StarsView(Context context,InformerView informer) {
    super(context);
    mStars = new Star[STARS_COUNT];
    mLastYs = new float[EVENT_COUNT];
    mInformerView = informer;
    //mPopupView = popup;
    setOnTouchListener(this);
    setBackgroundColor(Color.TRANSPARENT);
    
    paintNumber.setColor(Color.WHITE);
    paintNumber.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
    paintNumber.setTypeface(Typeface.DEFAULT_BOLD);
    paintNumber.setAntiAlias(true);
    paintNumber.setTextAlign(Paint.Align.CENTER);
    
    mFontSize = paintNumber.getTextSize();
    
    mStarYellow = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_yellow);
    mStarYellowActive = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_yellow_pressed);
    mStarBlue = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_blue);
    mStarBlueActive = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_blue_pressed);
    mStarGrey = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_grey);
    mStarGreyActive = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_grey_pressed);
  }
  // index
  int prev_star_index = 0;
  int curr_star_index = 0;
  //---------------------------------------------------------------------------
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    // текущие координаты пальца
    int x = (int)event.getX();
    int y = (int)event.getY();

    // определяем на какой звезде находится палец
    int star_index = 1;
    for(int i=0;i<mStars.length;i++)
      if(mStars[i]._rect.contains(x,y)) {
        star_index = mStars[i]._index;
        curr_star_index = i;
      }
    
    if(curr_star_index!=prev_star_index) {
      mStars[prev_star_index].pressed=false;
      prev_star_index = curr_star_index;
    }
    
    //mPopupView.setText(""+star_index);
    
    switch(event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        //mPopupView.setVisibility(View.VISIBLE);
        mStars[curr_star_index].pressed=true;
        mInformerView.setVisible(true);
        for(int i = 0; i < EVENT_COUNT; i++)
          mLastYs[i] = y;
        break;
      case MotionEvent.ACTION_MOVE:
        mStars[curr_star_index].pressed=true;
        mInformerView.setVisible(true);
        for (int i = 0; i < EVENT_COUNT-1; i++)
          mLastYs[i] = mLastYs[i+1];
        mLastYs[EVENT_COUNT-1] = y;
        break;
      case MotionEvent.ACTION_UP:
        //mPopupView.setVisibility(View.INVISIBLE);
        mStars[curr_star_index].pressed=false;
        mInformerView.setVisible(false);
        for(int i = 0; i < EVENT_COUNT; i++)
          mLastYs[i] = -100;
        ((DatingLayout)getParent()).onRate(star_index);
        //((DatingActivity)getContext()).doRate(0,star_index); // Опасная операция, требует рефакторинг !!!!
        break;
      default:
        //mPopupView.setVisibility(View.INVISIBLE);
        mStars[curr_star_index].pressed=false;
        mInformerView.setVisible(false);
        for(int i = 0; i < EVENT_COUNT; i++)
          mLastYs[i] = -100;
        break;
    }
    
    float averageY = 0.0f;
    for(int i = 0; i < EVENT_COUNT; i++)
      averageY += mLastYs[i];
    averageY /= EVENT_COUNT;
    
    mInformerView.setData(averageY,star_index);
    mInformerView.invalidate();
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
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    int height = MeasureSpec.getSize(heightMeasureSpec); // вычисляем предоставленную нам высоту для отрисовки
    int width  = (int)(mStarYellow.getWidth() * 1.3);         // вычисление своей ширины, вынести в константы !!!

    setMeasuredDimension(width,height);
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
    
    mStars[0] = new Star(x,y,star_w,star_h,10); y+=star_h; // смещение для отрисовки следующей звезды
    mStars[1] = new Star(x,y,star_w,star_h,9);  y+=star_h;
    mStars[2] = new Star(x,y,star_w,star_h,8);  y+=star_h;
    mStars[3] = new Star(x,y,star_w,star_h,7);  y+=star_h;
    mStars[4] = new Star(x,y,star_w,star_h,6);  y+=star_h;
    mStars[5] = new Star(x,y,star_w,star_h,5);  y+=star_h;
    mStars[6] = new Star(x,y,star_w,star_h,4);  y+=star_h;
    mStars[7] = new Star(x,y,star_w,star_h,3);  y+=star_h;
    mStars[8] = new Star(x,y,star_w,star_h,2);  y+=star_h;
    mStars[9] = new Star(x,y,star_w,star_h,1);
  }
  //---------------------------------------------------------------------------
}
