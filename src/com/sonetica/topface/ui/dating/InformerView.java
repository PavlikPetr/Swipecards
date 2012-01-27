package com.sonetica.topface.ui.dating;

import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class InformerView extends ViewGroup {
  // Data
  Rect mRect;
  int _x;
  int _y;
  String text = "text";
  Button btn1;
  Button btn2;
  private int mWidth = 150;
  // Constants
  private static final Paint informerPaint = new Paint();
  //---------------------------------------------------------------------------
  public InformerView(Context context) {
    super(context);
    informerPaint.setColor(Color.GREEN);
    setBackgroundColor(Color.DKGRAY);
    
    btn1 = new Button(context);
    btn1.setText("profile");
    btn1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getContext(),"size:"+btn1.getWidth()+" 2:"+btn1.getMeasuredWidth(),Toast.LENGTH_SHORT).show();
      }
    });
    addView(btn1);
    
    btn2 = new Button(context);
    btn2.setText("chat");
    addView(btn2);

  }
  //---------------------------------------------------------------------------
  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    canvas.drawText(text,_x,_y,informerPaint);
  }
  //---------------------------------------------------------------------------
  public int getWidthEx() {
    return mWidth;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int l,int t,int r,int b) {
    //super.layout(l,t,r,b);
    //mRect = new Rect(l,t,r,b);
    int w = btn1.getMeasuredWidth();
    int h = btn1.getMeasuredHeight();
    int y = r-w;
    int z = b-h;
    //btn1.layout(0,0,100,50);
    //btn1.layout(0,50,50,100);

  }
  //-------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    
    int width0  = MeasureSpec.getSize(widthMeasureSpec);
    int height0 = MeasureSpec.getSize(heightMeasureSpec);
    //btn1.measure(width0/2,height0/2);
    
    setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
  }
  //---------------------------------------------------------------------------
  public void setPosition(int x,int y) {
    _x = x; _y = y;
  }
  //---------------------------------------------------------------------------
}
