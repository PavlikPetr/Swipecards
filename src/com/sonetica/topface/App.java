package com.sonetica.topface;

import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.Utils;
import android.app.Application;

public class App extends Application {
  // Data
  public static final String TAG = "TopFace"; 
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Utils.log(this,"+onCreate");
    
    //DbaseManager db = new DbaseManager(this);
    
    CacheManager.init(this);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onTerminate() {
    Utils.log(this,"-onTerminate");
    super.onTerminate();
  }
  //---------------------------------------------------------------------------
}
