package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class InformerView extends ViewGroup {
  //---------------------------------------------------------------------------
  // class Informer
  //---------------------------------------------------------------------------
  class Informer {
    public float  _x;         // х
    public float  _y;         // у
    public int    _widht;     // ширина
    public int    _temp;      // temp
    public int    _height;    // высота
    public int    _index;     // цифра рейтинга
    public int    _bottom;    // нижняя граница предка 
    public String _text;      // текст подсказка
    public boolean _visible;      // рисовать или нет
    private int color = Color.WHITE;  // Битмап для бекграунда
    // Ctor
    public Informer(int widht,int height) {
      _widht  = widht;
      _height = height;
      _text   = "оценка ";
      _temp   = height / 2;
      informerPaint.setColor(Color.WHITE);
      informerTitlePaint.setColor(Color.BLACK);
    }
    public void draw(Canvas canvas) {
//      if(!_visible) return;
//      _y-=_temp;
//      if(_y<0) _y=0;
//      else if(_y>_bottom-_height) _y=_bottom-_height; // иметь просчитанным !!!
//      Debug.log(">>>>>>>>","_y:"+_y+",Y:"+(_bottom-_height));
//      canvas.drawRect(_x,_y,_x+_widht,_y+_height,informerPaint);
//      canvas.drawText(_text+_index,_x+15,_y+15,informerTitlePaint);
    }
  }
  //---------------------------------------------------------------------------
  // Data
  Button   mProfileBtn;
  Button   mChatBtn;
  Informer mInformer;
  // Constants
  private static final Paint informerTitlePaint = new Paint();
  private static final Paint informerPaint = new Paint();
  //---------------------------------------------------------------------------
  public InformerView(Context context) {
    super(context);
    
    setBackgroundColor(Color.TRANSPARENT);
    
    // Chat btn
    mChatBtn = new Button(context);
    mChatBtn.setBackgroundResource(R.drawable.dating_chat_selector);
    mChatBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((DatingLayout)getParent()).onChatBtnClick();
      }
    });
    addView(mChatBtn);
    
    // Profile btn
    mProfileBtn = new Button(context);
    mProfileBtn.setBackgroundResource(R.drawable.dating_profile_selector);
    mProfileBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((DatingLayout)getParent()).onProfileBtnClick();
      }
    });
    addView(mProfileBtn);
    
    // Informer popup
    mInformer = new Informer(200,80);

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
    int width  = mInformer._widht;                       // вычисление своей ширины
    int height = MeasureSpec.getSize(heightMeasureSpec); // вычисляем предоставленную нам высоту для отрисовки
    
    // передаем свои размеры предкам
    mChatBtn.measure(width,height);
    mProfileBtn.measure(width,height);
    
    setMeasuredDimension(width,height);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    int width  = getMeasuredWidth();
    int height = getMeasuredHeight();
    
    mInformer._bottom = height;
    
    int x = (int)(width  - mProfileBtn.getMeasuredWidth()*1.2);
    int y = (int)(height - mProfileBtn.getMeasuredHeight()*2.4);
    mChatBtn.layout(x,y,x+mChatBtn.getMeasuredWidth(),y+mChatBtn.getMeasuredHeight());
    
    y = (int)(y+mChatBtn.getMeasuredHeight()*1.2);
    mProfileBtn.layout(x,y,x+mProfileBtn.getMeasuredWidth(),y+mProfileBtn.getMeasuredHeight());
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
}
