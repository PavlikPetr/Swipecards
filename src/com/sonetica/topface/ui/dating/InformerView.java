package com.sonetica.topface.ui.dating;

import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.Button;

public class InformerView extends ViewGroup {
  // Data
  Rect mRect;
  int _x;
  int _y;
  String text = "text";
  Button btn;
  // Constants
  public static final int I = 50;
  private static final Paint informerPaint = new Paint();
  //---------------------------------------------------------------------------
  public InformerView(Context context) {
    super(context);
    informerPaint.setColor(Color.GREEN);
    setBackgroundColor(Color.MAGENTA);
    btn = new Button(context);
    btn.setText("profile");
    addView(btn);
  }
  //---------------------------------------------------------------------------
  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    canvas.drawText(text,_x,_y,informerPaint);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int l,int t,int r,int b) {
    //super.layout(l,t,r,b);
    //mRect = new Rect(l,t,r,b);
    Debug.log(this,">IV onLayout,l:"+l+" t:"+t+" r:"+r+" b:"+b);
    //btn.layout(l,t+40,l+80,t+40+40);
    //btn.measure(40,40);
  }
  //-------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    int width0  = MeasureSpec.getSize(widthMeasureSpec);
    int height0 = MeasureSpec.getSize(heightMeasureSpec);
    Debug.log(this,">IV onMeasure, w:"+width0+" h:"+height0);
  }
  //---------------------------------------------------------------------------
  public void setPosition(int x,int y) {
    _x = x; _y = y;
  }
  //---------------------------------------------------------------------------
}
