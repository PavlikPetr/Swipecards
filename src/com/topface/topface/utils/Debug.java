package com.topface.topface.utils;

import android.util.Log;
import com.topface.topface.App;

public class Debug {
  public static void log(Object obj,String msg) {
    if(obj == null)
      Log.i(App.TAG,"::" + msg);
    else if(obj instanceof String)
      Log.i(App.TAG,obj + "::" + msg);
    else
      Log.i(App.TAG,obj.getClass().getSimpleName() + "::" + msg);
  }
}
