package com.sonetica.topface;

import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.Debug;
import android.app.Application;
import android.content.Intent;

public class App extends Application {
  // Data
  public static final String TAG = "TopFace";
  public static final String SHARED_PREFERENCES_TAG = "preferences";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log(this,"=======================================");
    Debug.log(this,"+onCreate");
    
    //DbaseManager db = new DbaseManager(this);
    CacheManager.create(this);
    
    startService(new Intent(this, ConnectionService.class));
  }
  //---------------------------------------------------------------------------
  @Override
  public void onTerminate() {
    Debug.log(this,"-onTerminate");
    super.onTerminate();
  }
  //---------------------------------------------------------------------------
}
