package com.sonetica.topface;

import com.sonetica.topface.utils.Debug;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class App extends Application {
  // Data
  public static String SSID = "";
  // Constants
  public static final String TAG = "TopFace";
  public static final String SHARED_PREFERENCES_TAG = "preferences";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log(this,"=======================================");
    Debug.log(this,"+onCreate");
    
    App.loadSSID(this);
  }
  //---------------------------------------------------------------------------
  public static void saveSSID(Context context,String ssid) {
    if(ssid==null) ssid = "";
    
    SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(context.getString(R.string.s_ssid),ssid);
    editor.commit();
    
    SSID = ssid;
  }
  //---------------------------------------------------------------------------
  public static String loadSSID(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    return SSID = preferences.getString(context.getString(R.string.s_ssid),"");
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
    super.onTerminate();
  }
  //---------------------------------------------------------------------------
}
