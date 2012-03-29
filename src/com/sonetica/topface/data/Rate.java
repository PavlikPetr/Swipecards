package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.utils.Debug;

public class Rate extends AbstractData {
  // Data
  public int uid;                // идентификатор фотографии в альбоме пользвоателя
  public int age;                // возраст пользователя
  public int rate;               // оценка пользователя
  public int city_id;            // идентификатор города отправителя оценки
  public int unread_count;       // количество оставшихся непрочитанных
  public long created;           // время отправления оценки
  public boolean online;         // флаг нахождения пользователя в онлайне
  public boolean unread;         // флаг прочитанной оценки
  public String city_name;       // название города пользователя
  public String city_full;       // полное название города пользвоателя
  public String first_name;      // имя пользователя
  public String avatars_big;     // большая аватарка пользователя
  public String avatars_small;   // маленькая аватарка пользователя
  //---------------------------------------------------------------------------
  public static LinkedList<Rate> parse(ApiResponse response) {
    LinkedList<Rate> ratesList = new LinkedList<Rate>();
    
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0)
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Rate rate = new Rate();
          rate.first_name = item.optString("first_name");
          rate.online     = item.optBoolean("online");
          rate.unread     = item.optBoolean("unread");
          rate.created    = item.optLong("created"); // время приходит в секундах  *1000
          rate.unread_count = response.mJSONResult.optInt("unread");
          rate.uid        = item.optInt("uid");
          rate.age        = item.optInt("age");
          rate.rate       = item.optInt("rate");
          
          // city  
          JSONObject city = item.getJSONObject("city");
            rate.city_id    = city.optInt("id");            
            rate.city_name  = city.optString("name");
            rate.city_full  = city.optString("full");
            
          //  avatars
          JSONObject avatar  = item.getJSONObject("avatars");
            rate.avatars_small = avatar.optString("small");
            rate.avatars_big   = avatar.optString("big");
          ratesList.add(rate);
        }
    } catch(Exception e) {
      Debug.log("Rate.class","Wrong response parsing: " + e);
    }
    
    return ratesList;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return avatars_big;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return avatars_small;
  }
  //---------------------------------------------------------------------------
}
