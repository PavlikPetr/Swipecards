package com.sonetica.topface;

import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.Utils;
import android.app.Application;
import android.content.SharedPreferences;

public class App extends Application {
  // Data
  public static final String TAG = "TopFace";
  public static final String SHARED_PREFERENCES_TAG = "preferences";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Utils.log(this,"=======================================");
    Utils.log(this,"+onCreate");
    
    //DbaseManager db = new DbaseManager(this);
    CacheManager.create(this);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onTerminate() {
    Utils.log(this,"-onTerminate");
    super.onTerminate();
  }
  //---------------------------------------------------------------------------
}
