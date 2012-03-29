package com.sonetica.topface.services;

import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import com.sonetica.topface.utils.Memory;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class StatisticService extends Service {
  // Data
  //private TextView mText;
  //private WindowManager mWindowManager;
  private boolean mRunning;
  private Runnable mLooper;
  private Handler mStatHandler;
  private static final long TIMER = 1000L * 5;
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    Debug.log(this,"+onCreate");
    mLooper = new RunTask();
    mStatHandler = new Handler();
    mStatHandler.postDelayed(mLooper,TIMER);
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
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    mRunning = true;
    return START_STICKY;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    mRunning = false;
    mStatHandler.removeCallbacks(mLooper);
    mStatHandler.removeCallbacksAndMessages(StatisticService.class);
    mStatHandler = null;
    mLooper = null;
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
  // class RunTask
  //---------------------------------------------------------------------------
  class RunTask implements Runnable {
    public void run() {
      if(!mRunning) 
        return;
      
      Debug.log(null,"Ha:" + Memory.getHeapUsed()   + " Hf:" + Memory.getHeapFree() + "\n" + 
          "::Na:" + Memory.getNativeUsed() + " Nf:" + Memory.getNativeFree());
      Debug.log(null,"leaks manager: "+LeaksManager.getInstance().checkLeaks().size());

      if(mStatHandler!=null)
        mStatHandler.postDelayed(this,TIMER);
    }
  }
  //---------------------------------------------------------------------------
}//StatisticService
