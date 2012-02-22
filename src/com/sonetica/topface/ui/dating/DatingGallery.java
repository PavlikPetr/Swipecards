package com.sonetica.topface.ui.dating;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;

public class DatingGallery extends Gallery implements View.OnTouchListener {
  // Data
  //---------------------------------------------------------------------------
  public DatingGallery(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return true;
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onTouch(View arg0,MotionEvent event) {
    return false;
  }
  //---------------------------------------------------------------------------
}
