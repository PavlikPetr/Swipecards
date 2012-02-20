package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
  private static Bitmap mFrameBitmap;
  private static Bitmap mEroInfo;
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
    s_PaintBg.setAlpha(200);
    
    if(mFrameBitmap==null)
      mFrameBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.profile_frame_gallery);
    if(mEroInfo==null)
      mEroInfo = BitmapFactory.decodeResource(getResources(),R.drawable.profile_ero_info);
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
    canvas.drawRect(0,0,getWidth(),getHeight(),s_PaintBg);
    canvas.drawBitmap(mEroInfo,0,0,null);
    // cost
    canvas.drawText(""+cost,(float)(mEroInfo.getWidth()/2.2),(float)(mEroInfo.getHeight()/2.5),s_PaintInfo);
    // likes
    canvas.drawText(""+likes,(float)(mEroInfo.getWidth()/3.8),(float)(mEroInfo.getHeight()/1.18),s_PaintInfo);
    // dislikes
    canvas.drawText(""+dislikes,(float)(mEroInfo.getWidth()/1.21),(float)(mEroInfo.getHeight()/1.18),s_PaintInfo);
  }
  //---------------------------------------------------------------------------  
}
