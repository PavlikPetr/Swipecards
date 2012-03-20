package com.sonetica.topface.ui.profile;

import com.sonetica.topface.ui.Recycle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FrameImageView extends ImageView {
  // Data
  public boolean mOnlineState; 
  //---------------------------------------------------------------------------
  public FrameImageView(Context context) {
    this(context,null);
  }
  //---------------------------------------------------------------------------
  public FrameImageView(Context context,AttributeSet attrs) {
    super(context,attrs);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    setMeasuredDimension(Recycle.s_ProfilePhotoFrame.getWidth(),Recycle.s_ProfilePhotoFrame.getHeight());
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    Drawable canvasDrawable = getDrawable();
    if(canvasDrawable == null) {
      int x = (Recycle.s_ProfilePhotoFrame.getWidth()-Recycle.s_People.getWidth())/2;
      int y = (Recycle.s_ProfilePhotoFrame.getHeight()-Recycle.s_People.getHeight())/2;
      canvas.drawBitmap(Recycle.s_People,x,y,null);
    }
      //try {
        /*
        BitmapDrawable drawable = (BitmapDrawable)canvasDrawable;
        Bitmap fullSizeBitmap = drawable.getBitmap();
        int scaledWidth  = getMeasuredWidth();
        int scaledHeight = getMeasuredHeight();

        Bitmap mScaledBitmap;
        if(scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight())
          mScaledBitmap = fullSizeBitmap;
        else
          mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap,scaledWidth,scaledHeight,true /* filter * /);

        canvas.drawBitmap(mScaledBitmap,0,0,null);
        */
        // фрейм с тенюшкой
        canvas.drawBitmap(Recycle.s_ProfilePhotoFrame,0,0,null);
        
        // online state                      // ЗАРАНИЕ ПРОСЧИТАТЬ КООРДИНАТЫ
        canvas.drawBitmap(mOnlineState ? Recycle.s_Online : Recycle.s_Offline,(int)(getWidth()-Recycle.s_Online.getWidth()*1.5),Recycle.s_Online.getHeight()/2,null);
        
      //} catch(Exception e) {
        //e.printStackTrace();
      //}
    //}
  }
  //---------------------------------------------------------------------------
}
