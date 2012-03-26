package com.sonetica.topface;

import java.util.LinkedList;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.Profile;

/*
 *   Сохранение данных в течении жизни процесса
 */
public class Data {
  // Data
  public static int s_gridColumn;
  public static int s_HeaderHeight;
  
  public static LinkedList<City>   s_CitiesList;
  public static LinkedList<Album>  s_PhotoAlbum;
  
  public static Profile s_Profile;
  
  public static Drawable s_OwnerDrw;
  public static Drawable s_UserDrw;

  // Data Profile
  public static int s_Power;
  public static int s_Money;
  public static int s_Rates;
  public static int s_Likes;
  public static int s_Messages;
  public static int s_AverageRate;
  
  // Topface ssid key
  public static String SSID = "";  // ключ для запросов к TP серверу
  //---------------------------------------------------------------------------
  public static boolean init(Context context) {
    SSID = Data.loadSSID(context);

    if(SSID!=null)
      return true;
    else
      return false;
  }
  //---------------------------------------------------------------------------
  public static void saveSSID(Context context,String ssid) {
    if(ssid==null || ssid.length()==0)
      SSID = "";
    else
      SSID = ssid;
    
    SharedPreferences preferences   = context.getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(context.getString(R.string.s_ssid),SSID);
    editor.commit();
  }
  //---------------------------------------------------------------------------
  public static String loadSSID(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SSID = preferences.getString(context.getString(R.string.s_ssid),"");
    
    return SSID;
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
  public static void setProfile(Profile profile) {
    s_Profile = profile;
    
    updateNotification(profile);
  }
  //---------------------------------------------------------------------------
  public static void updateNotification(Profile profile) {
    s_Power = profile.power;
    s_Money = profile.money;
    s_Rates = profile.unread_rates;
    s_Likes = profile.unread_likes;
    s_Messages = profile.unread_messages;
    s_AverageRate = profile.average_rate;
  }
  //---------------------------------------------------------------------------
  public static void clear() {
    if(s_PhotoAlbum!=null)
      s_PhotoAlbum.clear();
    s_PhotoAlbum = null;
    
    if(s_CitiesList!=null)
      s_CitiesList.clear();
    s_CitiesList = null;
    
    s_OwnerDrw = null;
    s_UserDrw  = null;
    
    s_Profile = null;
  }
  //---------------------------------------------------------------------------
}
