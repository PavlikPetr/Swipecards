package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.widget.ImageView;

public class ProfileThumbView extends ImageView {
  // Data
  public boolean mIsAddButton;
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
    
    if(mIsAddButton) {
      Paint paint = new Paint();
      paint.setTextAlign(Align.CENTER);
      paint.setColor(Color.DKGRAY);
      paint.setAntiAlias(true);
      paint.setTextSize(19);
      canvas.drawText(getContext().getString(R.string.profile_btn_add),getWidth()/2,getHeight()-paint.getTextSize(),paint);
    }
  }
  //---------------------------------------------------------------------------  
}
