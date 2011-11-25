package com.sonetica.topface.utils;

import java.util.HashMap;
import com.sonetica.topface.App;
import android.util.Log;

/*
 *  Набор вспомагательных функций
 */
public class Utils {
  //---------------------------------------------------------------------------
  public static void log(Object obj,String msg) {
    if(obj == null)
      Log.i(App.TAG,"::" + msg);
    else
      Log.i(App.TAG,obj.getClass().getName() + "::" + msg);
  }
  //---------------------------------------------------------------------------
  public static int unixtime(){
    return (int)(System.currentTimeMillis() / 1000L);
  }
  //---------------------------------------------------------------------------
  public static HashMap<String, String> parseQueryString(String query) {
    String[] params = query.split("&");
    HashMap<String, String> map = new HashMap<String, String>();
    for(String param : params) {
      String name  = param.split("=")[0];
      String value = param.split("=")[1];
      map.put(name, value);
    }
    return map;
  }
  //---------------------------------------------------------------------------
}
