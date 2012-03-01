package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Like extends AbstractData {
  // Data
  //public int id;
  public int uid;                // идентификатор фотографии в альбоме пользователя
  public int age;                // возраст пользователя
  public int city_id;            // идентификатор города отправителя оценки
  public int unread_count;       // количество оставшихся непрочитанных
  public boolean online;         // флаг нахождения пользователя в онлайне
  public boolean unread;         // флаг прочитанного лайка
  public String city_name;       // название города пользователя
  public String city_full;       // полное название города пользвоателя
  public String first_name;      // имя пользователя
  public String avatars_big;     // большая аватарка пользователя
  public String avatars_small;   // маленькая аватарка пользователя
  //---------------------------------------------------------------------------
  public static LinkedList<Like> parse(Response response) {
    LinkedList<Like> likesList = new LinkedList<Like>();
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0)
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Like like = new Like();
            like.first_name  = item.getString("first_name");
            like.uid         = item.getInt("uid");
            like.age         = item.getInt("age");
            like.unread_count = response.mJSONResult.getInt("unread");
            like.online      = item.getBoolean("online");
            like.unread      = item.getBoolean("unread");
          // city  
          JSONObject city = item.getJSONObject("city");
            like.city_id    = city.getInt("id");            
            like.city_name  = city.getString("name");
            like.city_full  = city.getString("full");
          // avatars
          JSONObject avatar  = item.getJSONObject("avatars");
            like.avatars_big   = avatar.getString("big");
            like.avatars_small = avatar.getString("small");
          likesList.add(like);
        }
    } catch(JSONException e) {
      Debug.log("Like.class","Wrong response parsing: " + e);
    }
    return likesList;
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
