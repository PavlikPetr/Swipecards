package com.sonetica.topface.ui.profile.gallery;

import com.sonetica.topface.R;
import com.sonetica.topface.ui.Recycle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.widget.ImageView;

public class ProfileEroThumbView extends ImageView {
  // Data
  public int cost;       // стоимость просмотра фотографии
  public int likes;      // количество одобрительных отзывов
  public int dislikes;   // количество отрицательных отзывов
  public boolean mOwner;
  public boolean mIsAddButton;
  private static Paint s_PaintBg;
  private static Paint s_PaintInfo;
  //---------------------------------------------------------------------------
  public ProfileEroThumbView(Context context) {
    super(context);
    
    s_PaintInfo = new Paint();
    s_PaintInfo.setTextAlign(Align.RIGHT);
    s_PaintInfo.setColor(Color.WHITE);
    s_PaintInfo.setAntiAlias(true);
    s_PaintInfo.setTypeface(Typeface.DEFAULT_BOLD);
    
    s_PaintBg = new Paint();
    s_PaintBg.setColor(Color.WHITE);
    s_PaintBg.setAlpha(175);
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
    
    if(mOwner) {
      if(mIsAddButton) {
        Paint paint = new Paint();
        paint.setTextAlign(Align.CENTER);
        paint.setColor(Color.DKGRAY);
        paint.setAntiAlias(true);
        paint.setTextSize(19);
        canvas.drawText(getContext().getString(R.string.profile_btn_add),getWidth()/2,getHeight()-paint.getTextSize(),paint);
      }
      
      canvas.drawBitmap(Recycle.s_ProfileGalleryFrame,0,0,null);
      
      return;
    }
    
    // белая полупрозрачная пелена
    canvas.drawRect(0,0,getWidth(),getHeight(),s_PaintBg);
    
    // рамка для стоимости и лайков эро фотки
    int o = 2;
    canvas.drawBitmap(Recycle.s_ProfileEroInfo,0,o,null);
    // cost
    canvas.drawText(""+cost,(float)(Recycle.s_ProfileEroInfo.getWidth()/2.2),o+(float)(Recycle.s_ProfileEroInfo.getHeight()/2.5),s_PaintInfo);
    // likes
    canvas.drawText(""+likes,(float)(Recycle.s_ProfileEroInfo.getWidth()/3.8),o+(float)(Recycle.s_ProfileEroInfo.getHeight()/1.18),s_PaintInfo);
    // dislikes
    canvas.drawText(""+dislikes,(float)(Recycle.s_ProfileEroInfo.getWidth()/1.21),o+(float)(Recycle.s_ProfileEroInfo.getHeight()/1.18),s_PaintInfo);
    
    canvas.drawBitmap(Recycle.s_ProfileGalleryFrame,0,0,null);
  }
  //---------------------------------------------------------------------------  
}
