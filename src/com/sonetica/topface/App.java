

package com.sonetica.topface;

import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import android.app.Application;

/*
 *  Контекст приложения
 */
public class App extends Application {
  // Data
  public static int     state;    // стейт работы приложения
  public static boolean cached;   // отображать данные из кеша
  public static boolean isActive; // активность для потока получения непрочитанных данных
  // Working states
  public static int T_RUNNING  = 0;
  public static int T_SLEEPING = 1;
  // Constants
  public static final String TAG = "TopFace";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log(this,"=======================================");
    Debug.log(this,"+onCreate");
    
    // App initialization
    Global.init(this);
    Device.init(this);
    Data.init(this);
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
