package com.sonetica.topface;

import com.sonetica.topface.utils.Debug;
import android.app.Application;

/*
 *  Контекст приложения
 */
public class App extends Application {
  // Constants
  public static final String TAG = "TopFace";
  public static final String SHARED_PREFERENCES_TAG = "preferences";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log(this,"=======================================");
    Debug.log(this,"+onCreate");
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
