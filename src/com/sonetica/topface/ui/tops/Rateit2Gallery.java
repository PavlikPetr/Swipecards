package com.sonetica.topface.ui.tops;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class Rateit2Gallery extends Gallery {
  // Data
  //---------------------------------------------------------------------------
  public Rateit2Gallery(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  //---------------------------------------------------------------------------
  public Rateit2Gallery(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return true;
  }
  //---------------------------------------------------------------------------
}
