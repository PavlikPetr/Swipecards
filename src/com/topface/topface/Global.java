package com.topface.topface;

import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import android.content.Context;
import android.os.Build;

/*
 *   Глобальные переменные и стейты 
 */
public class Global {
  // Read_only Data
  public static int API_VERSION;                // поддерживаемая версия серверного апи
  public static String API_URL;                 // урл сервера топфейса для запросов
  public static String EXTERANAL_CACHE_DIR;     // путь для кеша на внешней карте памяти
  public static String SHARED_PREFERENCES_TAG;  // имя файла общих настроек
  public static String TOKEN_PREFERENCES_TAG;   // имя файла хранения токена
  public static String PROFILE_PREFERENCES_TAG; // имя файла хранения профиля
  public static String LOCALE;                  // язык пользователя в приложении
  public static String CLIENT_TYPE;             // тип клиента
  public static String CLIENT_VERSION;          // версия клиента
  public static String CLIENT_DEVICE;           // тип устройста
  public static String CLIENT_ID;               // id устройства пользователя
  public static int PHOTO_WIDTH;                // максимально разрешение для отправки фото на ТФ сервер
  public static int PHOTO_HEIGHT;               // максимально разрешение для отправки фото на ТФ сервер
  public static int GRID_COLUMN;                // число колонок в грид контролах
  // Constants
  public static final int GIRL = 0;
  public static final int BOY  = 1;
  // add photo params
  //---------------------------------------------------------------------------
  public static boolean init(Context context) {
    try {
      API_VERSION             = context.getResources().getInteger(R.integer.api_version);
      API_URL                 = context.getString(R.string.api_url);
      EXTERANAL_CACHE_DIR     = context.getString(R.string.sdcard_root_path) + context.getString(R.string.sdcard_cache_path);
      SHARED_PREFERENCES_TAG  = context.getString(R.string.general_preferences_name);
      PROFILE_PREFERENCES_TAG = context.getString(R.string.profile_preferences_name);
      TOKEN_PREFERENCES_TAG   = context.getString(R.string.token_preferences_name);
      LOCALE                  = context.getApplicationContext().getResources().getConfiguration().locale.getLanguage();
      CLIENT_VERSION          = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
      CLIENT_TYPE             = context.getString(R.string.platform);
      CLIENT_DEVICE           = Build.BRAND + " " + Build.MANUFACTURER;
      CLIENT_ID               = Build.ID;
      PHOTO_WIDTH             = context.getResources().getInteger(R.integer.photo_width);
      PHOTO_HEIGHT            = context.getResources().getInteger(R.integer.photo_height);

      switch(Device.width) {
        case Device.W_240:
        case Device.W_320:
          GRID_COLUMN = 2;
          break;
        case Device.W_480:
          GRID_COLUMN = 3;
          break;
        default:
          GRID_COLUMN = 4;
          break;
      }
      
    } catch (Exception e) {
      Debug.log("Global","init exception:" + e);
      return false;
    }
    return true;
  }
  //---------------------------------------------------------------------------
}
