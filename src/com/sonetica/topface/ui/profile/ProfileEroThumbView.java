package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.widget.ImageView;

public class ProfileEroThumbView extends ImageView {
  // Data
  public int cost;       // стоимость просмотра фотографии
  public int likes;      // количество одобрительных отзывов
  public int dislikes;   // количество отрицательных отзывов
  public boolean mOwner;
  public boolean mIsAddButton;
  private static Bitmap mFrameBitmap;
  private static Bitmap mEroInfo;
  private static Bitmap mPeopleBitmap;
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
    
    if(mFrameBitmap==null)
      mFrameBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.profile_frame_gallery);
    if(mEroInfo==null)
      mEroInfo = BitmapFactory.decodeResource(getResources(),R.drawable.profile_ero_info);
    if(mPeopleBitmap==null)
      mPeopleBitmap  = BitmapFactory.decodeResource(getResources(),R.drawable.icon_people);
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
    
    Drawable canvasDrawable = getDrawable();
    if(canvasDrawable==null && mIsAddButton!=true) {
      int x = (mFrameBitmap.getWidth()-mPeopleBitmap.getWidth())/2;
      int y = (mFrameBitmap.getHeight()-mPeopleBitmap.getHeight())/2;
      canvas.drawBitmap(mPeopleBitmap,x,y,null);
    }
    
    if(mOwner) {
      if(mIsAddButton) {
        Paint paint = new Paint();
        paint.setTextAlign(Align.CENTER);
        paint.setColor(Color.DKGRAY);
        paint.setAntiAlias(true);
        paint.setTextSize(19);
        canvas.drawText(getContext().getString(R.string.profile_btn_add),getWidth()/2,getHeight()-paint.getTextSize(),paint);
      }
      return;
    }
    
    // белая полупрозрачная пелена
    canvas.drawRect(0,0,getWidth(),getHeight(),s_PaintBg);
    
    // рамка для стоимости и лайков эро фотки
    int o = 2;
    canvas.drawBitmap(mEroInfo,0,o,null);
    // cost
    canvas.drawText(""+cost,(float)(mEroInfo.getWidth()/2.2),o+(float)(mEroInfo.getHeight()/2.5),s_PaintInfo);
    // likes
    canvas.drawText(""+likes,(float)(mEroInfo.getWidth()/3.8),o+(float)(mEroInfo.getHeight()/1.18),s_PaintInfo);
    // dislikes
    canvas.drawText(""+dislikes,(float)(mEroInfo.getWidth()/1.21),o+(float)(mEroInfo.getHeight()/1.18),s_PaintInfo);
    
    canvas.drawBitmap(mFrameBitmap,0,0,null);
  }
  //---------------------------------------------------------------------------  
}
