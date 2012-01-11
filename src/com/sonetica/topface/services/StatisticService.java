package com.sonetica.topface.services;

import com.sonetica.topface.utils.Debug;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class StatisticService extends Service {
  // Data
  //private TextView mText;
  //private WindowManager mWindowManager;
  private StatisticHandler mStatHandler;
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    Debug.log(this,"+onCreate");
    /*
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
    */
    mStatHandler = new StatisticHandler();
    mStatHandler.sendEmptyMessage(0);
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    //mWindowManager.removeView(mText);
    //mWindowManager = null;
    mStatHandler = null;
    //mText = null;
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
  //---------------------------------------------------------------------------
  //class StatisticHandler
  //---------------------------------------------------------------------------
  class StatisticHandler extends Handler{
    public StatisticHandler() {
      Debug.log(this,"+started");
    }
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      /*
      mText.post(new Runnable() {
        @Override
        public void run() {
          mText.setText("Ha:" + Memory.getHeapUsed() + "  Hf:" + Memory.getHeapFree() + "\n" + 
                        "Na:" + Memory.getNativeUsed() + "  Nf:" + Memory.getNativeFree());
        }
      });
      */
      /*
      Debug.log(null,"Ha:" + Memory.getHeapUsed()   + " Hf:" + Memory.getHeapFree() + "\n" + 
                     "::Na:" + Memory.getNativeUsed() + " Nf:" + Memory.getNativeFree());
      */
      mStatHandler.sendEmptyMessageDelayed(0,1000*4);
    }
  }//StatisticHandler
  //---------------------------------------------------------------------------
}//StatisticService
