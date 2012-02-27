package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FrameImageView extends ImageView {
  // Data
  public boolean mOnlineState; 
  private Bitmap mFrameBitmap;
  private Bitmap mOnlineBitmap;
  private Bitmap mOfflineBitmap;
  //---------------------------------------------------------------------------
  public FrameImageView(Context context) {
    this(context,null);
  }
  //---------------------------------------------------------------------------
  public FrameImageView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    mFrameBitmap   = BitmapFactory.decodeResource(getResources(),R.drawable.profile_frame_photo);
    mOnlineBitmap  = BitmapFactory.decodeResource(getResources(),R.drawable.im_online);
    mOfflineBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.im_offline);
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
    //Drawable canvasDrawable = getDrawable();
    //if(canvasDrawable != null) {
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
        canvas.drawBitmap(mFrameBitmap,0,0,null);
        
        // online state                      // ЗАРАНИЕ ПРОСЧИТАТЬ КООРДИНАТЫ
        canvas.drawBitmap(mOnlineState ? mOnlineBitmap : mOfflineBitmap,(int)(getWidth()-mOnlineBitmap.getWidth()*1.5),mOnlineBitmap.getHeight()/2,null);
        
      //} catch(Exception e) {
        //e.printStackTrace();
      //}
    //}
  }
  //---------------------------------------------------------------------------
}
