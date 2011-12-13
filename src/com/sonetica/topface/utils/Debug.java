package com.sonetica.topface.utils;

import android.util.Log;
import com.sonetica.topface.App;

public class Debug {
  public static void log(Object obj,String msg) {
    if(obj == null)
      Log.i(App.TAG,"::" + msg);
    else
      Log.i(App.TAG,obj.getClass().getName() + "::" + msg);
  }
}
