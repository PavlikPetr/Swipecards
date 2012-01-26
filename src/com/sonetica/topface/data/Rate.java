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
  public String first_name;     // имя пользователя
  public int age;               // возраст пользователя
  public int rate;              // оценка пользователя
  public boolean online;        // флаг нахождения пользователя в онлайне
  public boolean unread;        // флаг прочитанной оценки
  public int created;           // время отправления оценки
  public String city_id;        // идентификатор города отправителя оценки
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
          rate.created    = item.getInt("created");
          rate.uid        = item.getInt("uid");
          rate.age        = item.getInt("age");
          rate.rate       = item.getInt("rate");
          rate.city_id    = item.getString("city_id");
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
}
