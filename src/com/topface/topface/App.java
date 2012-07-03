package com.topface.topface;

import android.content.Context;
import com.topface.topface.utils.Debug;
import android.app.Application;

//@ReportsCrashes(formKey="dEdjcUtaMmJqNmlSdlZmUTlwejlXUlE6MQ")
public class App extends Application {
  // Constants
  public static final String TAG = "TopFace";
  public static final boolean DEBUG = false;
  private static Context mContext;
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    mContext = getApplicationContext();
    Debug.log("App","+onCreate");
    //ACRA.init(this);
    Data.init(getApplicationContext());
    Recycle.init(getApplicationContext());
  }

  public static Context getContext() {
      return mContext;
  }
  //---------------------------------------------------------------------------
}

/*
// status bar height
19px for LDPI
25px for MDPI
38px for HDPI
50px for XHDPI
*/