package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageViewEx extends ImageView {
  // Data
  //private static final int mRadius = 10;
  private static Bitmap mFrameBitmap;
  //---------------------------------------------------------------------------
  public RoundedImageViewEx(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  public RoundedImageViewEx(Context context,AttributeSet attrs) {
    super(context,attrs);
    if(mFrameBitmap==null)
      mFrameBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.chat_frame_photo);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    setMeasuredDimension(mFrameBitmap.getWidth(),mFrameBitmap.getHeight());
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    Drawable canvasDrawable = getDrawable();
    if(canvasDrawable != null) {
      try {
        BitmapDrawable drawable = (BitmapDrawable)canvasDrawable;
        Bitmap fullSizeBitmap = drawable.getBitmap();
        int scaledWidth  = getMeasuredWidth();
        int scaledHeight = getMeasuredHeight();
        Bitmap mScaledBitmap;
        if(scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight())
          mScaledBitmap = fullSizeBitmap;
        else
          mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap,scaledWidth,scaledHeight,true /* filter */);

        Bitmap roundBitmap = mScaledBitmap; //Utils.getRoundedCornerBitmap(mScaledBitmap,mScaledBitmap.getWidth(),mScaledBitmap.getHeight(),mRadius);
        canvas.drawBitmap(roundBitmap,0,0,null);
        
        // тенюшка
        canvas.drawBitmap(mFrameBitmap,0,0,null); 
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  //---------------------------------------------------------------------------
}
