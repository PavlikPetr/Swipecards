package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class InformerView extends ViewGroup {
  // class
  class Informer {
    private float _x;         // х
    private float _y;         // у
    private int   _widht;     // ширина
    private int   _height;    // высота
    private int   _index;     // цифра рейтинга
    private String _text;     // текст подсказка
    int color = Color.WHITE;  // Битмап для бекграунда
    // Ctor
    public Informer(int x,int y,int widht,int height) {
      _x = 0; 
      _y = y;
      _widht  = widht;
      _height = height;
      _text   = "оценка ";
      informerPaint.setColor(Color.WHITE);
      informerTitlePaint.setColor(Color.BLACK);
    }
    public void draw(Canvas canvas) {
      canvas.drawRect(_x,_y,_x+_widht,_y+_height,informerPaint);
      canvas.drawText(_text+_index,_x+15,_y+15,informerTitlePaint);
    }
    public int getWidth() {
      return _widht;
    }
    public int getHeight() {
      return _height;
    }
    public void setData(float y,int index) {
      _y = y;
      _index = index;
    }
  }
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
    mChatBtn.setBackgroundResource(R.drawable.btn_dating_chat_selector);
    mChatBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getContext(),"Chat",Toast.LENGTH_SHORT).show();
      }
    });
    addView(mChatBtn);
    
    // Profile btn
    mProfileBtn = new Button(context);
    mProfileBtn.setBackgroundResource(R.drawable.btn_dating_profile_selector);
    mProfileBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getContext(),"Profile",Toast.LENGTH_SHORT).show();
      }
    });
    addView(mProfileBtn);
    
    // Informer popup
    mInformer = new Informer(-100,-100,100,50);

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
    int width  = mInformer.getWidth();                   // вычисление своей ширины
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
    
    int x = (int)(width  - mProfileBtn.getMeasuredWidth()*1.6);
    int y = (int)(height - mProfileBtn.getMeasuredHeight()*3);
    mChatBtn.layout(x,y,x+mChatBtn.getMeasuredWidth(),y+mChatBtn.getMeasuredHeight());
    
    y = (int)(y+mChatBtn.getMeasuredHeight()*1.5);
    mProfileBtn.layout(x,y,x+mProfileBtn.getMeasuredWidth(),y+mProfileBtn.getMeasuredHeight());
  }
  //---------------------------------------------------------------------------
  public void setData(float y,int index) {
    mInformer.setData(y,index);
  }
  //---------------------------------------------------------------------------
}
