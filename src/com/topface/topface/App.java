package com.topface.topface;

import com.topface.topface.utils.Debug;
import android.app.Application;

//@ReportsCrashes(formKey="dEdjcUtaMmJqNmlSdlZmUTlwejlXUlE6MQ")
public class App extends Application {
  // Constants
  public static final String TAG = "TopFace";
  public static final boolean DEBUG = false;
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log("App","+onCreate");
    //ACRA.init(this);
    Data.init(getApplicationContext());
    Recycle.init(getApplicationContext());
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