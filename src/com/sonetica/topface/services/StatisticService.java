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
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class StatisticService extends Service {
  // Data
  private StatisticDrawer mStatDrawer;
  private WindowManager mWindowManager;
  private TextView mText;
  private int counter;
  //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onStart(Intent paramIntent, int paramInt) {
    
    mText = new TextView(this);
    mText.setTextSize(6);
    mText.setTextColor(Color.YELLOW);
    mText.setTypeface(Typeface.MONOSPACE);
    
    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
        LayoutParams.WRAP_CONTENT, 
        LayoutParams.WRAP_CONTENT,
        190,
        -360,
        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);
    mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
    mWindowManager.addView(mText,lp);
    
    mStatDrawer = new StatisticDrawer();
    mStatDrawer.sendEmptyMessage(0);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    mStatDrawer.close();
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  //class StatisticDrawer Handler
  class StatisticDrawer extends Handler{
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
