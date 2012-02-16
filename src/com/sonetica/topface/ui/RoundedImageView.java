package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
  // Data
  private int mFrameType;
  //private static final int mRadius = 10;
  private static Bitmap mDialogBitmap;
  private static Bitmap mChatBitmap;
  // Frames Type
  private static final int DIALOG = 0;
  private static final int CHAT   = 1;
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
    
    if(mDialogBitmap==null && mFrameType==DIALOG)
      mDialogBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.im_frame_photo);
    else if(mChatBitmap==null && mFrameType==CHAT)
      mChatBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.chat_frame_photo);

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
    if(mFrameType==DIALOG)
      setMeasuredDimension(mDialogBitmap.getWidth(),mDialogBitmap.getHeight());
    else if(mFrameType==CHAT)
      setMeasuredDimension(mChatBitmap.getWidth(),mChatBitmap.getHeight());
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    Drawable canvasDrawable = getDrawable();
    if(canvasDrawable != null) {
      try {
        Bitmap fullSizeBitmap = ((BitmapDrawable)canvasDrawable).getBitmap();
        int scaledWidth  = getMeasuredWidth();
        int scaledHeight = getMeasuredHeight();
        Bitmap mScaledBitmap;
        if(scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight())
          mScaledBitmap = fullSizeBitmap;
        else
          mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap,scaledWidth,scaledHeight,true /* filter */);

        // перенес скругление в менеджер
        //Bitmap roundBitmap = Utils.getRoundedCornerBitmap(mScaledBitmap,mScaledBitmap.getWidth(),mScaledBitmap.getHeight(),mRadius);
        
        canvas.drawBitmap(mScaledBitmap,0,0,null);
        
        // фрейм с тенюшкой
        if(mFrameType==DIALOG)
          canvas.drawBitmap(mDialogBitmap,0,0,null);
        if(mFrameType==CHAT)
          canvas.drawBitmap(mChatBitmap,0,0,null); 
        
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  //---------------------------------------------------------------------------
}
