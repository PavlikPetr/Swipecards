package com.sonetica.topface;

import com.sonetica.topface.R;
import android.content.Context;
import android.content.SharedPreferences;

/*
 *   Глобальные переменные и стейты 
 */
public class Global {
  // Data
  public static int run;  // стейт работы приложения
  public static String SSID = ""; // ключ для запросов к TP серверу
  // Constants
  public static int ERROR_SERVICE = 1;
  // Methods
  //---------------------------------------------------------------------------
  public static void init() {
    // todo something
    // init os version
  }
  //---------------------------------------------------------------------------
  public static void saveSSID(Context context,String ssid) {
    if(ssid==null)
      ssid = "";
    
    SharedPreferences preferences = context.getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(context.getString(R.string.s_ssid),ssid);
    editor.commit();
    
    SSID = ssid;
  }
  //---------------------------------------------------------------------------
  public static String loadSSID(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    return SSID = preferences.getString(context.getString(R.string.s_ssid),"");
  }
  //---------------------------------------------------------------------------  
}
