package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.widget.ImageView;

public class ProfileThumbView extends ImageView {
  // Data
  public boolean mEro;
  private static Bitmap mFrameBitmap;
  //---------------------------------------------------------------------------
  public ProfileThumbView(Context context) {
    super(context);
    
    if(mFrameBitmap==null)
      mFrameBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.profile_frame_gallery);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    setMeasuredDimension(mFrameBitmap.getWidth(),mFrameBitmap.getHeight());
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawBitmap(mFrameBitmap,0,0,null);
  }
  //---------------------------------------------------------------------------  
}
