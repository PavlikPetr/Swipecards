package com.sonetica.topface.services;

import com.sonetica.topface.utils.Memory;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class StatisticService extends Service {
  // Data
  private StatisticHandler mStatDrawer;
  private WindowManager mWindowManager;
  private TextView mText;
  //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onStart(Intent paramIntent, int paramInt) {
    
    mText = new TextView(this);
    mText.setTextSize(12);
    mText.setTextColor(Color.YELLOW);
    mText.setTypeface(Typeface.MONOSPACE);
    
    mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
    Display display = mWindowManager.getDefaultDisplay();

    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
        LayoutParams.WRAP_CONTENT, 
        LayoutParams.WRAP_CONTENT,
        0,
        display.getHeight()-50,
        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, 
        PixelFormat.TRANSLUCENT);
    
    mWindowManager.addView(mText,lp);
   
    mStatDrawer = new StatisticHandler();
    mStatDrawer.sendEmptyMessage(0);

    Toast.makeText(this,"service start",Toast.LENGTH_LONG).show();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    Toast.makeText(this,"service destroy",Toast.LENGTH_LONG).show();
    mStatDrawer.close();
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  //class StatisticDrawer Handler
  class StatisticHandler extends Handler{
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);      
      mText.post(new Runnable() {
        @Override
        public void run() {
          mText.setText("Ha:" + Memory.getHeapUsed() + "  Hf:" + Memory.getHeapFree() + "\n" + 
                        "Na:" + Memory.getNativeUsed() + "  Nf:" + Memory.getNativeFree());
        }
      });
      mStatDrawer.sendEmptyMessageDelayed(0,1000*1);
    }
    public void close() {
      mWindowManager.removeView(mText);
    }
  }
  //---------------------------------------------------------------------------
}
