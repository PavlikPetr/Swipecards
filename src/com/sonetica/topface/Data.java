package com.sonetica.topface;

import java.util.LinkedList;
import android.content.Context;
import android.content.SharedPreferences;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.data.TopUser;

/*
 *   Сохранение данных в течении жизни процесса
 */
public class Data {
  // Data
  public static String SSID;  // ключ для запросов к TP серверу
  // Data Profile
  public static int _rates;
  public static int _messages;
  public static int _likes;
  public static int _power;
  public static int _money;
  
  public static LinkedList<Inbox>   s_InboxList;
  public static LinkedList<Like>    s_LikesList;
  public static LinkedList<TopUser> s_TopsList;
  public static LinkedList<Rate>    s_RatesList;
  public static LinkedList<City>    s_CitiesList;
  //---------------------------------------------------------------------------
  public static void init(Context context) {
    
    SSID = Data.loadSSID(context);
    
    s_InboxList  = new LinkedList<Inbox>();
    s_LikesList  = new LinkedList<Like>();
    s_TopsList   = new LinkedList<TopUser>();
    s_RatesList  = new LinkedList<Rate>();
    s_CitiesList = new LinkedList<City>();
  }
  //---------------------------------------------------------------------------
  public static void updateNews(Profile profile) {
    if(profile==null) {
      _rates = _likes = _messages = _money = _power = 0;
      return;
    }
    _rates    = profile.unread_rates;
    _likes    = profile.unread_likes;
    _messages = profile.unread_messages;
    //mPower    = profile.power;
    //mMoney    = profile.money;
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
  }
  //---------------------------------------------------------------------------
}
