package com.sonetica.topface.ui.dating;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;

class TopfaceView extends ImageView {
  // Data
  public  int money;
  public  int power;
  public  int age;
  public  String name;
  public  String city;
  public  String status;
  public  boolean online;
  private int _visible;
  // Constants  
  private static final Paint paint = new Paint();
  //----------------------------------
  public TopfaceView(Context context) {
    super(context);
    paint.setColor(Color.YELLOW);
    paint.setTextSize(20);
  }
  //----------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    if(_visible==View.INVISIBLE)
      return;
    
    canvas.drawText(""+money,20,60,paint);
    canvas.drawText(""+money,50,60,paint);
    canvas.drawText(name+", "+age,0,90,paint);
    canvas.drawText(""+online,150,90,paint);
    canvas.drawText(city,0,120,paint);
    canvas.drawText(status,0,140,paint);
  }
  //----------------------------------
  public void visible(int visible) {
    _visible = visible;
  }
  //----------------------------------
  public int isVisible() {
    return _visible;
  }  
  //----------------------------------
}
