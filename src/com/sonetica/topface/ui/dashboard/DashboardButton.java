package com.sonetica.topface.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.Button;

/*
 *  Класс для кнопок на Dashboard активити (НЕ ИСПОЛЬЗУЕТСЯ)
 *  для отображения поверх кнопки иконки кол-во сообщений, оценок и т.д.
 */
public class DashboardButton extends Button {
  // Data
  //---------------------------------------------------------------------------
  public DashboardButton(Context context,AttributeSet attrs) {
    super(context,attrs);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
  }
  //--------------------------------------------------------------------------- 
}
