package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Rate extends AbstractData {
  // Data
  public int uid;               // идентификатор фотографии в альбоме пользвоателя
  public int age;               // возраст пользователя
  public int rate;              // оценка пользователя
  public int city_id;           // идентификатор города отправителя оценки
  public int unread_count;      // количество оставшихся непрочитанных
  public long created;          // время отправления оценки
  public boolean online;        // флаг нахождения пользователя в онлайне
  public boolean unread;        // флаг прочитанной оценки
  public String first_name;     // имя пользователя
  public String avatars_big;    // большая аватарка пользователя
  public String avatars_small;  // маленькая аватарка пользователя
  //---------------------------------------------------------------------------
  public static LinkedList<Rate> parse(Response response) {
    LinkedList<Rate> ratesList = new LinkedList<Rate>();
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0)
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Rate rate = new Rate();
          rate.first_name = item.getString("first_name");
          rate.online     = item.getBoolean("online");
          rate.unread     = item.getBoolean("unread");
          rate.created    = item.getLong("created")*1000; // время приходит в секундах
          rate.unread_count = response.mJSONResult.getInt("unread");
          rate.uid        = item.getInt("uid");
          rate.age        = item.getInt("age");
          rate.rate       = item.getInt("rate");
          rate.city_id    = item.getInt("city_id");
          JSONObject avatar  = item.getJSONObject("avatars");
          rate.avatars_small = avatar.getString("small");
          rate.avatars_big   = avatar.getString("big");
          ratesList.add(rate);
        }
    } catch(JSONException e) {
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
