package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.Button;

public class InformerView extends ViewGroup {
  //---------------------------------------------------------------------------
  // class Informer
  //---------------------------------------------------------------------------
  class Informer {            // информер нажатой звезды
    public float  _x;         // х
    public float  _y;         // у
    public int    _widht;     // ширина
    public int    _temp;      // temp
    public int    _height;    // высота
    public int    _index;     // цифра рейтинга
    public int    _bottom;    // нижняя граница предка 
    public String _text;      // текст подсказка
    public boolean _visible;  // рисовать или нет
    // Ctor
    public Informer(int widht,int height) {
      _widht  = widht;
      _height = height;
      _text   = "оценка ";
      _temp   = height / 2;
    }
    public void draw(Canvas canvas) {
      if(!_visible) 
        return;
      
      _y-=_temp;                      // ПЕРЕПИСАТЬ ОТРИСОВКУ 
      
      if(_y<0) 
        _y=0;
      else if(_y>_bottom-_height) 
        _y=_bottom-_height; // иметь просчитанным !!!
      
      int z = (mBkgrnd.getHeight()-mStar.getHeight())/2;
      canvas.drawBitmap(mBkgrnd,_x,_y,informerPaint);
      canvas.drawBitmap(mStar,_x+z*2,_y+z,informerPaint);
      canvas.drawText(""+_index,(float)(_x+mStar.getWidth()/2)+z*2,(float)(_y+z+mStar.getHeight()/1.6),informerTitlePaint);
      if(_index==10) {
        canvas.drawText("1",(float)(_x+mStar.getWidth())+z*5,(float)(_y+z+mStar.getHeight()/1.6),informerTitlePaint);
        // иметь просчитанным !!!
        int n = mBkgrnd.getWidth()-mMoney.getWidth()-z*2;
        z = (mBkgrnd.getHeight()-mMoney.getHeight())/2;
        canvas.drawBitmap(mMoney,_x+n,_y+z,informerPaint);
      }
      
    }
  }
  //---------------------------------------------------------------------------
  // Data
  private Button   mProfileBtn; // кнопка на профиль
  private Button   mChatBtn;    // кнопка в чат
  private Informer mInformer;   // информер текущей звезды
  // Bitmaps
  private Bitmap   mBkgrnd;     // Битмап для бекграунда
  private Bitmap   mStar;       // звезда
  private Bitmap   mMoney;      // монетка
  // Constants
  private Paint informerTitlePaint = new Paint();
  private Paint informerPaint = new Paint();
  //---------------------------------------------------------------------------
  public InformerView(Context context) {
    super(context);
    
    setBackgroundColor(Color.TRANSPARENT);
    
    mBkgrnd = BitmapFactory.decodeResource(getResources(),R.drawable.dating_popup);
    mStar   = BitmapFactory.decodeResource(getResources(),R.drawable.dating_star_yellow);
    mMoney  = BitmapFactory.decodeResource(getResources(),R.drawable.dating_money);
    
    informerTitlePaint.setColor(Color.WHITE);
    informerTitlePaint.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
    informerTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
    informerTitlePaint.setAntiAlias(true);
    informerTitlePaint.setTextAlign(Paint.Align.CENTER);
    
    // Chat btn
    mChatBtn = new Button(context);
    mChatBtn.setId(R.id.chatBtn);
    mChatBtn.setBackgroundResource(R.drawable.dating_chat_selector);
    addView(mChatBtn);
    
    // Profile btn
    mProfileBtn = new Button(context);
    mProfileBtn.setId(R.id.profileBtn);
    mProfileBtn.setBackgroundResource(R.drawable.dating_profile_selector);
    addView(mProfileBtn);
    
    // Informer popup
    mInformer = new Informer(mBkgrnd.getWidth(),mBkgrnd.getHeight());
  }
  //---------------------------------------------------------------------------
  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    mInformer.draw(canvas);
  }
  //-------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    int width  = (int)(mBkgrnd.getWidth()*1.1);          // вычисление своей ширины
    int height = MeasureSpec.getSize(heightMeasureSpec); // вычисляем предоставленную нам высоту для отрисовки
    
    // передаем свои размеры предкам
    mChatBtn.measure(width,height);
    mProfileBtn.measure(width,height);
    
    setMeasuredDimension(width,height);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    
    float offset_from_stars = 1.3f;
    
    int width  = getMeasuredWidth();
    int height = getMeasuredHeight();
    
    mInformer._bottom = height;
    
    int x = (int)(width - mProfileBtn.getMeasuredWidth()*offset_from_stars);
    int y = (int)(height - mProfileBtn.getMeasuredHeight()*2.5);
    mChatBtn.layout(x,y,x+mChatBtn.getMeasuredWidth(),y+mChatBtn.getMeasuredHeight());
    
    y = (int)(y + mChatBtn.getMeasuredHeight()*offset_from_stars);
    mProfileBtn.layout(x,y,x + mProfileBtn.getMeasuredWidth(),y + mProfileBtn.getMeasuredHeight());
  }
  //---------------------------------------------------------------------------
  public void setVisible(boolean visible) {
    mInformer._visible = visible;
  }
  //---------------------------------------------------------------------------
  public void setData(float y,int index) {
    mInformer._y = y;
    mInformer._index = index;
  }
  //---------------------------------------------------------------------------
  public void setBlock(boolean block) {
    mChatBtn.setEnabled(block);
    mProfileBtn.setEnabled(block);
  }
  //---------------------------------------------------------------------------
  public void release() {
    mProfileBtn = null;
    mChatBtn = null;
    mInformer = null;
    
    if(mBkgrnd!=null)
      mBkgrnd.recycle();
    mBkgrnd=null;
    
    if(mStar!=null)
      mStar.recycle();
    mStar=null;
    
    if(mMoney!=null)
      mMoney.recycle();
    mMoney=null;
    
    informerTitlePaint = null;
    informerPaint = null;
  }
  //---------------------------------------------------------------------------
}
