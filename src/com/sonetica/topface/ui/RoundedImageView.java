package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
  // Data
  private int mFrameType;
  // Frames Type
  private static final int INBOX = 0;
  private static final int CHAT  = 1;
  //---------------------------------------------------------------------------
  public RoundedImageView(Context context) {
    this(context,null);
  }
  //---------------------------------------------------------------------------
  public RoundedImageView(Context context,AttributeSet attrs) {
    this(context,attrs,0);
  }
  //---------------------------------------------------------------------------
  public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context,attrs,defStyle);
    setAttrs(attrs);
  }
  //---------------------------------------------------------------------------
  private void setAttrs(AttributeSet attrs) {
    if(attrs==null)
      return;
    
    TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.RoundedImageView, 0,0);
    mFrameType = a.getInteger(R.styleable.RoundedImageView_frame,0);
    a.recycle();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    if(mFrameType==INBOX)
      setMeasuredDimension(Recycle.s_InboxFrame.getWidth(),Recycle.s_InboxFrame.getHeight());
    else if(mFrameType==CHAT)
      setMeasuredDimension(Recycle.s_ChatFrame.getWidth(),Recycle.s_ChatFrame.getHeight());
    else
      setMeasuredDimension(0,0);
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    Drawable canvasDrawable = getDrawable();
    if(canvasDrawable == null)
      return;
    
    // Frame
    Bitmap frameBitmap = mFrameType==INBOX ? Recycle.s_InboxFrame : Recycle.s_ChatFrame;
    
    // people
    int x = (frameBitmap.getWidth()-Recycle.s_People.getWidth())/2;
    int y = (frameBitmap.getHeight()-Recycle.s_People.getHeight())/2;
    canvas.drawBitmap(Recycle.s_People,x,y,null);

    try {
      Bitmap fullSizeBitmap = ((BitmapDrawable)canvasDrawable).getBitmap();
      
      if(fullSizeBitmap==null) {
        // фрейм с тенюшкой
        canvas.drawBitmap(frameBitmap,0,0,null);
      }
      
      int scaledWidth  = getMeasuredWidth();
      int scaledHeight = getMeasuredHeight();
      Bitmap mScaledBitmap;
      if(scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight())
        mScaledBitmap = fullSizeBitmap;
      else
        mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap,scaledWidth,scaledHeight,true);

      // перенес скругление в менеджер
      //Bitmap roundBitmap = Utils.getRoundedCornerBitmap(mScaledBitmap,mScaledBitmap.getWidth(),mScaledBitmap.getHeight(),mRadius);
      
      canvas.drawBitmap(mScaledBitmap,0,0,null);
      
      // фрейм с тенюшкой
      canvas.drawBitmap(frameBitmap,0,0,null);
      
    } catch(Exception e) {
      //
    }
  }
  //---------------------------------------------------------------------------
}
