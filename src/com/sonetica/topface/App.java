package com.sonetica.topface;

import com.sonetica.topface.utils.Debug;
import android.app.Application;
//import org.acra.ACRA;
//import org.acra.annotation.ReportsCrashes;

/*
 *    Контекст приложения
 */
//@ReportsCrashes(formKey="dEFCekVNeDJEaDZHcjQyU1k2ZWtTbGc6MQ")
public class App extends Application {
  // Data
  public static final String TAG = "TopFace";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    
    Debug.log(this,"+onCreate");
    
//    ACRA.init(this);
  }
  //---------------------------------------------------------------------------
}
