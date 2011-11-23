package com.sonetica.topface.ui.tops;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

public class TopButton extends ImageView {
  // Data
  public Bitmap  mBitmap;
  public Boolean mLikeState;
  public String  mPercent;
  
  private static Paint s_Paint;
  private static Paint s_PaintLine;
  private static Paint s_PaintText;
  static {
    s_Paint = new Paint();
    s_Paint.setColor(Color.DKGRAY);

    s_PaintLine = new Paint();
    s_PaintLine.setColor(Color.BLACK);
    s_PaintLine.setAlpha(150);
    
    s_PaintText = new Paint();
    s_PaintText.setColor(Color.WHITE);
  }
  //---------------------------------------------------------------------------
  public TopButton(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    // SCALING

    if(mBitmap!=null) {
      canvas.drawBitmap(mBitmap,(getWidth()-mBitmap.getWidth())/2,(getHeight()-mBitmap.getHeight())/2,s_Paint);
      Rect lineRect = new Rect(0,getHeight()-29,getWidth(),getHeight());
      canvas.drawRect(lineRect,s_PaintLine);
      canvas.drawText("пыщ",lineRect.left+15,lineRect.top+15,s_PaintText);
    } else
      canvas.drawRect(new Rect(0,0,getWidth(),getHeight()),s_Paint);
  }
  //---------------------------------------------------------------------------
  @Override
  public void setImageBitmap(Bitmap bitmap) {
    mBitmap = bitmap;
  }
  //---------------------------------------------------------------------------  
}
