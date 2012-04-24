package com.topface.topface;

import com.topface.topface.services.NotificationService;
import com.topface.topface.ui.Recycle;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/*
 *    Контекст приложения
 */
@ReportsCrashes(formKey="dEdjcUtaMmJqNmlSdlZmUTlwejlXUlE6MQ")
public class App extends Application {
  // Data
  public static boolean init;
  // SSID key
  public static String SSID;
  // Constants
  public static final String TAG = "TopFace";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log("App","+onCreate");
    ACRA.init(this);
    App.init(this);
  }
  //---------------------------------------------------------------------------
  public static void init(Context context) {
    // ssid
    loadSSID(context);

    // App init
    init = Device.init(context);
    init = Global.init(context);
    init = Recycle.init(context);
    init = CacheProfile.init(context);

    if(!init) return;
    
    context.startService(new Intent(context,NotificationService.class));
    //context.startService(new Intent(context,StatisticService.class));
  }
  //---------------------------------------------------------------------------
  public static String loadSSID(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SSID = preferences.getString(context.getString(R.string.s_ssid),"");
    
    return SSID;
  }
  //---------------------------------------------------------------------------
  public static void saveSSID(Context context,String ssid) {
    SSID = (ssid==null || ssid.length()==0) ? "" : ssid;
    
    SharedPreferences preferences = context.getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(context.getString(R.string.s_ssid),SSID);
    editor.commit();
  }
  //---------------------------------------------------------------------------
  public static void removeSSID(Context context) {
    SSID = "";
    
    SharedPreferences preferences   = context.getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(context.getString(R.string.s_ssid),SSID);
    editor.commit();
  }
  //---------------------------------------------------------------------------
}


// поиск запущенного сервиса
// при запуске сервиса в другом процессе
// стартует второй апп для инициализации контекста
// Data.SSID рассинхрон между процессами
/*
 ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
 for(RunningAppProcessInfo app : manager.getRunningAppProcesses())
  if(app.processName.equals("com.topface.topface:notification"))
    return;
*/