package com.sonetica.topface;

import com.sonetica.topface.R;
import android.content.Context;
import android.os.Build;

/*
 *   Глобальные переменные и стейты 
 */
public class Global {
  // Read_only Data
  public static String API_URL;                // урл сервера топфейса для запросов
  public static String EXTERANAL_CACHE_DIR;    // путь для кеша на внешней карте памяти
  public static String SHARED_PREFERENCES_TAG; // имя файла общих настроек
  public static String TOKEN_PREFERENCES_TAG;  // имя файла хранения токена
  public static String LOCALE;                 // язык пользователя в приложении
  public static String CLIENT_TYPE;            // тип клиента
  public static String CLIENT_VERSION;         // версия клиента
  public static String CLIENT_DEVICE;          // тип устройста
  public static String CLIENT_ID;              // id устройства пользователя
  //---------------------------------------------------------------------------
  public static void init(Context context) {
    API_URL                = context.getString(R.string.api_url);
    EXTERANAL_CACHE_DIR    = context.getString(R.string.sdcard_root_path)+context.getString(R.string.sdcard_cache_path);
    SHARED_PREFERENCES_TAG = context.getString(R.string.general_preferences_name);
    TOKEN_PREFERENCES_TAG  = context.getString(R.string.token_preferences_name);
    LOCALE                 = "ru"; //context.getApplicationContext().getResources().getConfiguration().locale.getLanguage();
    CLIENT_TYPE            = "android";
    CLIENT_VERSION         = "0.1";
    CLIENT_DEVICE          = Build.BRAND + " " + Build.MANUFACTURER;
    CLIENT_ID              = Build.ID;
  }
  //---------------------------------------------------------------------------  
}
