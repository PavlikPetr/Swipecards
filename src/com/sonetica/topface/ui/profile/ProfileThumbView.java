package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import com.sonetica.topface.ui.Recycle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ProfileThumbView extends ImageView {
  // Data
  public boolean mIsAddButton;
  //---------------------------------------------------------------------------
  public ProfileThumbView(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    setMeasuredDimension(Recycle.s_ProfileGalleryFrame.getWidth(),Recycle.s_ProfileGalleryFrame.getHeight());
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    Drawable canvasDrawable = getDrawable();
    if(canvasDrawable==null && mIsAddButton!=true) {
      int x = (Recycle.s_ProfileGalleryFrame.getWidth()-Recycle.s_People.getWidth())/2;
      int y = (Recycle.s_ProfileGalleryFrame.getHeight()-Recycle.s_People.getHeight())/2;
      canvas.drawBitmap(Recycle.s_People,x,y,null);
    }
    
    canvas.drawBitmap(Recycle.s_ProfileGalleryFrame,0,0,null);
    
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
