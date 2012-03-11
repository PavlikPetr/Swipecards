package com.sonetica.topface.ui;

import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MyView extends View implements View.OnTouchListener {
  //Data
  Paint paint = new Paint();
  //---------------------------------------------------------------------------
  public MyView(Context context) {
    super(context);
    setBackgroundColor(Color.WHITE);
    setOnTouchListener(this);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawRect(0,0,getWidth(),getHeight(),paint);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    //super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    int width  = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    
    setMeasuredDimension(width*2,height*2);
    
    Debug.log(this,">> MyView onMeasure, w:"+width+" h:"+height);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    //super.onLayout(changed,left,top,right,bottom);
    
    Debug.log(this,">> MyView onLayout,l:"+left+" t:"+top+" r:"+right+" b:"+bottom);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onTouch(View v,MotionEvent event) {
    int action = event.getAction();
    switch(action) {
      case MotionEvent.ACTION_DOWN:
        Toast.makeText(getContext(),"down J",Toast.LENGTH_SHORT).show();
        break;
    }
    return true;
  }
  //---------------------------------------------------------------------------  
}
