package com.topface.topface.ui.views;

import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.utils.CacheProfile;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

public class StarsView extends View implements View.OnTouchListener {
  //---------------------------------------------------------------------------
  // interface RateListener
  //---------------------------------------------------------------------------
  public interface OnRateListener {
    public void onRate(int rate);
  }
  //---------------------------------------------------------------------------
  // calss Star
  //---------------------------------------------------------------------------
  class Star {
    public  int _index;    // оценка
    private int _x;
    private int _y;
    private int _bmp_y;
    private int _widht;
    private int _height;
    public Rect _rect;       
    public boolean pressed; // состояние
    // Ctor
    public Star(int x,int y,int width,int height,int index) {
      _x = x;
      _y = y;
      _bmp_y = y + ((height - Recycle.s_RateAverage.getHeight()) / 2);   // центрирование по вертикале
      _widht   = width;
      _height  = height;
      _index   = index;
      _rect = new Rect(_x,_y,_x+_widht,_y+_height);
    }
    public void draw(Canvas canvas) {
      if(_index==CacheProfile.average_rate) {  // средняя оценка
        if(!pressed)
          canvas.drawBitmap(Recycle.s_RateAverage,_x,_bmp_y,paintStar);
        else
          canvas.drawBitmap(Recycle.s_RateAveragePressed,_x,_bmp_y,paintStar);
        // Star Index         вынести расчеты !!!!!!!!
        canvas.drawText(""+_index,(float)(_x+Recycle.s_RateAverage.getWidth()/2),(float)(_bmp_y+Recycle.s_RateAverage.getHeight()/1.5),paintStarNumber);
      }
      
      if(_index>CacheProfile.average_rate && _index<10) { // high
        if(!pressed)
          canvas.drawBitmap(Recycle.s_RateHigh,_x,_bmp_y,paintStar);
        else
          canvas.drawBitmap(Recycle.s_RateHighPressed,_x,_bmp_y,paintStar);
        // Star Index         вынести расчеты !!!!!!!!
        canvas.drawText(""+_index,(float)(_x+Recycle.s_RateHigh.getWidth()/2),(float)(_bmp_y+Recycle.s_RateHigh.getHeight()/1.5),paintHeartNumber);
      }
      
      if(_index<CacheProfile.average_rate) { // low
        if(!pressed)
          canvas.drawBitmap(Recycle.s_RateLow,_x,_bmp_y,paintStar);
        else
          canvas.drawBitmap(Recycle.s_RateLowPressed,_x,_bmp_y,paintStar);
        // Star Index         вынести расчеты !!!!!!!!
        canvas.drawText(""+_index,(float)(_x+Recycle.s_RateLow.getWidth()/2),(float)(_bmp_y+Recycle.s_RateLow.getHeight()/1.5),paintStarNumber);
      }
      
      if(_index==10) { // 10 top
        if(!pressed)
          canvas.drawBitmap(Recycle.s_RateTop,_x,_bmp_y,paintStar);
        else
          canvas.drawBitmap(Recycle.s_RateTopPressed,_x,_bmp_y,paintStar);
        // Star Index         вынести расчеты !!!!!!!!
        canvas.drawText(""+_index,(float)(_x+Recycle.s_RateTop.getWidth()/2),(float)(_bmp_y+Recycle.s_RateTop.getHeight()/1.5),paintHeartNumber);
      }

    }
  }
  //---------------------------------------------------------------------------
  // Data
  //public int mAverageRate;   // средняя выставляемвя оценка
  private float[] mLastYs;   // массив последних нажатий
  private Star[]  mStars;    // статичный массив объектов для отрисовки звезд;
  private InformerView mInformerView;    // обсервер текущего нажатия на экран
  private OnRateListener mRateListener;  // listener на клик по звезде
  // Paints
  private Paint paintStar   = new Paint();
  private Paint paintStarNumber = new Paint();
  private Paint paintHeartNumber = new Paint();
  // Constants
  private static final int EVENT_COUNT = 3;   // число последних запоминаемых координат пальца
  private static final int STARS_COUNT = 10;  // кол-во звезд на фрейме
  //---------------------------------------------------------------------------
  public StarsView(Context context,InformerView informer) {
    super(context);
    mStars  = new Star[STARS_COUNT];
    mLastYs = new float[EVENT_COUNT];
    mInformerView = informer;
    
    //mAverageRate = CacheProfile.average_rate;
    
    setId(R.id.cntlStarsView);
    setOnTouchListener(this);
    setBackgroundColor(Color.TRANSPARENT);
    
    paintStarNumber.setColor(Color.DKGRAY);
    paintStarNumber.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
    paintStarNumber.setTypeface(Typeface.DEFAULT_BOLD);
    paintStarNumber.setAntiAlias(true);
    paintStarNumber.setTextAlign(Paint.Align.CENTER);
    
    paintHeartNumber.setColor(Color.WHITE);
    paintHeartNumber.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
    paintHeartNumber.setTypeface(Typeface.DEFAULT_BOLD);
    paintHeartNumber.setAntiAlias(true);
    paintHeartNumber.setTextAlign(Paint.Align.CENTER);
  }
  //---------------------------------------------------------------------------
  // index 
  int prev_star_index = 0;
  int curr_star_index = 0;
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    // текущие координаты пальца
    int x = (int)event.getX();
    int y = (int)event.getY();

    // находится ли палец в пределах контрола звезд
    boolean isStar = false;
    // определяем на какой звезде находится палец
    int star_index = 1;
    for(int i=0;i<mStars.length;i++)
      if(mStars[i]._rect.contains(x,y)) {
        star_index = mStars[i]._index;
        curr_star_index = i;
        isStar = true; 
      }
    
    // определение нажатой звезды
    if(curr_star_index!=prev_star_index) {
      mStars[prev_star_index].pressed = false;
      prev_star_index = curr_star_index;
    }
    
    switch(event.getAction()) {
      case MotionEvent.ACTION_DOWN:
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
        mStars[curr_star_index].pressed=false;
        mInformerView.setVisible(false);
        for(int i = 0; i < EVENT_COUNT; i++)
          mLastYs[i] = -100;
        
        // клик по звезде
        if(isStar)
          mRateListener.onRate(star_index);

        break;
      default:
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
    
    if(!isStar) {
      mInformerView.setVisible(false);
      mStars[curr_star_index].pressed = false;
    }
    
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
    int width  = (int)(Recycle.s_RateAverage.getWidth() * 1.1); // говорим, что нам нужно больше

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
  public void setOnRateListener(OnRateListener listener) {
    mRateListener = listener;
  }
  //---------------------------------------------------------------------------
  public void setBlock(boolean block) {
    //mAverageRate = CacheProfile.average_rate;    // Костыль по обновлению средней оценки
    this.setEnabled(block);
  }
  //---------------------------------------------------------------------------
  public void release() {
    mStars = null;
    mRateListener = null;
    paintStar = null;
    paintStarNumber = null;
  }
  //---------------------------------------------------------------------------
}
