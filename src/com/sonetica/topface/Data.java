package com.sonetica.topface;

import java.util.LinkedList;
import android.content.Context;
import android.content.SharedPreferences;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.data.TopUser;

/*
 *   Сохранение данных в течении жизни процесса
 */
public class Data {
  // Data
  private static LinkedList<SearchUser> s_SearchList;  // dating
  private static LinkedList<Inbox>      s_InboxList;
  private static LinkedList<Like>       s_LikesList;
  private static LinkedList<TopUser>    s_TopsList;
  private static LinkedList<Rate>       s_RatesList;
  private static LinkedList<City>       s_CitiesList;
  
  public static Profile s_Profile;
  // Data Profile
  public static int s_Rates;
  public static int s_Messages;
  public static int s_Likes;
  public static int s_Power;
  public static int s_Money;
  public static int s_AverageRate;
  // Topface ssid key
  public static String SSID;  // ключ для запросов к TP серверу
  //---------------------------------------------------------------------------
  public static void init(Context context) {
    
    SSID = Data.loadSSID(context);
    
    s_SearchList = new LinkedList<SearchUser>();
    s_InboxList  = new LinkedList<Inbox>();
    s_LikesList  = new LinkedList<Like>();
    s_TopsList   = new LinkedList<TopUser>();
    s_RatesList  = new LinkedList<Rate>();
    s_CitiesList = new LinkedList<City>();
  }
  //---------------------------------------------------------------------------
  public static void updateNotification(Profile profile) {
    if(profile==null) {
      s_Rates = s_Likes = s_Messages = s_Money = s_Power = s_AverageRate = 0;
      return;
    }
    s_Rates       = profile.unread_rates;
    s_Likes       = profile.unread_likes;
    s_Messages    = profile.unread_messages;
    s_Power       = profile.power;
    s_Money       = profile.money;
    s_AverageRate = profile.average_rate;
  }
  //---------------------------------------------------------------------------
  public static void saveSSID(Context context,String ssid) {
    if(ssid==null)
      ssid = "";
    
    SharedPreferences preferences   = context.getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(context.getString(R.string.s_ssid),ssid);
    editor.commit();
    
    SSID = ssid;
  }
  //---------------------------------------------------------------------------
  public static String loadSSID(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SSID = preferences.getString(context.getString(R.string.s_ssid),"");
    
    return SSID;
  }
  //---------------------------------------------------------------------------
  public static void clear() {
    s_SearchList.clear();
    s_SearchList = null;
    
    s_InboxList.clear();
    s_InboxList = null;
    
    s_LikesList.clear();
    s_LikesList = null;
    
    s_TopsList.clear();
    s_TopsList = null;
    
    s_RatesList.clear();
    s_RatesList = null;
    
    s_CitiesList.clear();
    s_CitiesList = null;

    s_Profile = null;
  }
  //---------------------------------------------------------------------------
}
