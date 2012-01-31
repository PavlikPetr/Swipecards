package com.sonetica.topface.ui.dashboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

// (НЕ ИСПОЛЬЗУЕТСЯ , заменен на TableLayout)
/*
 * Класс слоя для Dashboard активити для позиционирования элементов на экране
 * спизжен с хабра http://habrahabr.ru/blogs/android_development/130194
 */
public class DashboardLayout extends ViewGroup {
  // Data
  private int mMaxChildWidth;
  private int mMaxChildHeight;
  // Constants
  private static final int UNEVEN_GRID_PENALTY_MULTIPLIER = 10;
  //---------------------------------------------------------------------------
  public DashboardLayout(Context context) {
      super(context, null);
  }
  //---------------------------------------------------------------------------
  public DashboardLayout(Context context, AttributeSet attrs) {
      super(context, attrs, 0);
  }
  //---------------------------------------------------------------------------
  public DashboardLayout(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // widthMeasureSpec,heightMeasureSpec размеры, получаемые от родителя, которые можем занять
    
    mMaxChildWidth  = 0;
    mMaxChildHeight = 0;
    
    int count = getChildCount();
    

    int childWidthMeasureSpec  = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec),  MeasureSpec.AT_MOST);
    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST);

    // Поиск максимального размера среди детей
    for(int i=0;i<count;i++) {
      View child = getChildAt(i);
      if(child.getVisibility() == GONE)
        continue;
      
      child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
      
      mMaxChildWidth  = Math.max(mMaxChildWidth,  child.getMeasuredWidth());
      mMaxChildHeight = Math.max(mMaxChildHeight, child.getMeasuredHeight());
    }
    
    childWidthMeasureSpec  = MeasureSpec.makeMeasureSpec(mMaxChildWidth,  MeasureSpec.EXACTLY);
    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight, MeasureSpec.EXACTLY);
    
    // Установка максимального размера для всех детей
    for(int i=0;i<count;i++) {
      View child = getChildAt(i);
      if(child.getVisibility()==GONE)
        continue;
      child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
    
    // Обязательно должн быть вызван данный метод, устанавливает размеры занимаемые компонентом
    setMeasuredDimension(resolveSize(mMaxChildWidth, widthMeasureSpec),resolveSize(mMaxChildHeight, heightMeasureSpec));
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int width  = r - l;
    int height = b - t;

    final int count = getChildCount();

    // Подсчет числа видимых детей
    int visibleCount = 0;
    for(int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if(child.getVisibility() == GONE)
        continue;
      ++visibleCount;
    }

    if(visibleCount == 0)
      return;

    // Считает какое число строк и столбцов будет оптимизировано для каждого горизонт. и
    // вертикального whitespace между итемами. Start with a 1 x N grid, then try 2 x N, and so on.
    int bestSpaceDifference = Integer.MAX_VALUE;
    int spaceDifference;

    // Размеры горизонт и вертикал пространства между итемами 
    int hSpace = 0;
    int vSpace = 0;

    int cols = 1;
    int rows;

    while(true) {
      rows = (visibleCount - 1) / cols + 1;

      hSpace = ((width  - mMaxChildWidth  * cols) / (cols + 1));
      vSpace = ((height - mMaxChildHeight * rows) / (rows + 1));

      spaceDifference = Math.abs(vSpace - hSpace);
      if(rows * cols != visibleCount)
        spaceDifference *= UNEVEN_GRID_PENALTY_MULTIPLIER;

      if (spaceDifference < bestSpaceDifference) {
        // Found a better whitespace squareness/ratio
        bestSpaceDifference = spaceDifference;

        // If we found a better whitespace squareness and there's only 1 row, this is
        // the best we can do.
        if (rows == 1)
          break;
        
      } else {
        // This is a worse whitespace ratio, use the previous value of cols and exit.
        --cols;
        rows = (visibleCount - 1) / cols + 1;
        hSpace = ((width - mMaxChildWidth * cols) / (cols + 1));
        vSpace = ((height - mMaxChildHeight * rows) / (rows + 1));
        break;
      }
      ++cols;
    }

    // Lay out children based on calculated best-fit number of rows and cols.

    // If we chose a layout that has negative horizontal or vertical space, force it to zero.
    hSpace = Math.max(0, hSpace);
    vSpace = Math.max(0, vSpace);

    // Re-use width/height variables to be child width/height.
    width = (width - hSpace * (cols + 1)) / cols;
    height = (height - vSpace * (rows + 1)) / rows;

    int left, top;
    int col, row;
    int visibleIndex = 0;
    for(int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if(child.getVisibility() == GONE)
        continue;
      row = visibleIndex / cols;
      col = visibleIndex % cols;

      left = hSpace * (col + 1) + width * col;
      top = vSpace * (row + 1) + height * row;

      child.layout(left, top, 
                  (hSpace == 0 && col == cols - 1) ? r : (left + width),
                  (vSpace == 0 && row == rows - 1) ? b : (top + height));
      ++visibleIndex;
    }
  }
}//DashboardLayout
