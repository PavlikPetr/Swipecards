package com.sonetica.topface.ui;

import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class Jopa extends View implements View.OnTouchListener {
  //Data
  Paint paint = new Paint();
  //---------------------------------------------------------------------------
  public Jopa(Context context) {
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
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    //super.onLayout(changed,left,top,right,bottom);
    
    Debug.log(this,">> JOPA onLayout,l:"+left+" t:"+top+" r:"+right+" b:"+bottom);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    //super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    int width0  = MeasureSpec.getSize(widthMeasureSpec);
    int height0 = MeasureSpec.getSize(heightMeasureSpec);
    
    setMeasuredDimension(width0*2,height0*2);
    
    Debug.log(this,">> JOPA onMeasure, w:"+width0+" h:"+height0);
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
