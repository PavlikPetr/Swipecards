package com.sonetica.topface;

import com.sonetica.topface.utils.Debug;
import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/*
 *    Контекст приложения
 */
@ReportsCrashes(formKey="dEFCekVNeDJEaDZHcjQyU1k2ZWtTbGc6MQ")
public class App extends Application {
  // Data
  public static int state;       // стейт работы приложения
  public static boolean cached;  // отображать данные из кеша
  // Constants
  public static final String TAG = "TopFace";
  // Working states
  public static final int T_RUNNING  = 0;
  public static final int T_SLEEPING = 1;
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    
    Debug.log(this,"=======================================");
    Debug.log(this,"+onCreate");
    
    ACRA.init(this);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onLowMemory() {
    Debug.log(this,"onLowMemory");
  }
  //---------------------------------------------------------------------------
  @Override
  public void onTerminate() {
    Debug.log(this,"-onTerminate");
    Debug.log(this,"=======================================");
    
    super.onTerminate();
  }
  //---------------------------------------------------------------------------
}
