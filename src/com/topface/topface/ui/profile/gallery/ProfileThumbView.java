package com.topface.topface.ui.profile.gallery;

import com.topface.topface.R;
import com.topface.topface.Recycle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
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
    
    if(mIsAddButton!=true) {
      int x = (Recycle.s_ProfileGalleryFrame.getWidth()-Recycle.s_People.getWidth())/2;
      int y = (Recycle.s_ProfileGalleryFrame.getHeight()-Recycle.s_People.getHeight())/2;
      canvas.drawBitmap(Recycle.s_People,x,y,null);
    }
    
    super.onDraw(canvas);
    
    canvas.drawBitmap(Recycle.s_ProfileGalleryFrame, 0, 0, null);
    
    if(mIsAddButton) {
      Paint paint = new Paint();
      paint.setTextAlign(Align.CENTER);
      paint.setColor(Color.DKGRAY);
      paint.setAntiAlias(true);
      paint.setTextSize((14 * getResources().getDisplayMetrics().density + 0.5f));
      canvas.drawText(getContext().getString(R.string.profile_btn_add),getWidth()/2,getHeight()-paint.getTextSize(),paint);
    }
  }
  //---------------------------------------------------------------------------  
}
