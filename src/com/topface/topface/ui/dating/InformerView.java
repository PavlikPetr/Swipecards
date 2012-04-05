package com.topface.topface.ui.dating;

import com.topface.topface.R;
import com.topface.topface.ui.Recycle;
import android.content.Context;
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
    public boolean _visible;  // рисовать или нет
    // Ctor
    public Informer(int widht,int height) {
      _widht  = widht;
      _height = height;
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
      
      int z = (Recycle.s_StarPopupBG.getHeight()-Recycle.s_StarYellow.getHeight())/2;
      // background
      canvas.drawBitmap(Recycle.s_StarPopupBG,_x,_y,informerPaint);
      // star
      canvas.drawBitmap(Recycle.s_StarYellow,_x+z*2,_y+z,informerPaint);
      // rating
      canvas.drawText(""+_index,(float)(_x+Recycle.s_StarYellow.getWidth()/2)+z*2,(float)(_y+z+Recycle.s_StarYellow.getHeight()/1.6),informerTitlePaint);
      if(_index==10) {
        canvas.drawText("1",(float)(_x+Recycle.s_StarYellow.getWidth())+z*5,(float)(_y+z+Recycle.s_StarYellow.getHeight()/1.6),informerTitlePaint);
        // иметь просчитанным !!!
        int n = Recycle.s_StarPopupBG.getWidth()-Recycle.s_Money.getWidth()-z*2;
        z = (Recycle.s_StarPopupBG.getHeight()-Recycle.s_Money.getHeight())/2;
        // money
        canvas.drawBitmap(Recycle.s_Money,_x+n,_y+z,informerPaint);
      }
      
    }
  }
  //---------------------------------------------------------------------------
  // Data
  private Button   mProfileBtn; // кнопка на профиль
  private Button   mChatBtn;    // кнопка в чат
  private Informer mInformer;   // информер текущей звезды
  // Constants
  private Paint informerTitlePaint = new Paint();
  private Paint informerPaint = new Paint();
  //---------------------------------------------------------------------------
  public InformerView(Context context) {
    super(context);
    
    setBackgroundColor(Color.TRANSPARENT);
    
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
    mInformer = new Informer(Recycle.s_StarPopupBG.getWidth(),Recycle.s_StarPopupBG.getHeight());
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
    int width  = (int)(Recycle.s_StarPopupBG.getWidth()*1.1); // вычисление своей ширины
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
    informerTitlePaint = null;
    informerPaint = null;
  }
  //---------------------------------------------------------------------------
}
