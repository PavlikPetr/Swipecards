package com.sonetica.topface.ui.tops;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;

public class TopButton extends ImageView {
  // Data
  //---------------------------------------------------------------------------
  public TopButton(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    //canvas.drawBitmap(bitmap,left,top,paint);
  }
  //---------------------------------------------------------------------------
}
