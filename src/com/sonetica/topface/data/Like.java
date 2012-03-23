package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sonetica.topface.net.ApiResponse;
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
  public static LinkedList<Like> parse(ApiResponse response) {
    LinkedList<Like> likesList = new LinkedList<Like>();
    
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0)
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          Like like = new Like();
            like.first_name  = item.optString("first_name");
            like.uid         = item.optInt("uid");
            like.age         = item.optInt("age");
            like.unread_count = response.mJSONResult.getInt("unread");
            like.online      = item.optBoolean("online");
            like.unread      = item.optBoolean("unread");
            
          // city  
          JSONObject city = item.getJSONObject("city");
            like.city_id    = city.optInt("id");            
            like.city_name  = city.optString("name");
            like.city_full  = city.optString("full");
            
          // avatars
          JSONObject avatar  = item.getJSONObject("avatars");
            like.avatars_big   = avatar.optString("big");
            like.avatars_small = avatar.optString("small");
            
          likesList.add(like);
        }
    } catch(Exception e) {
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
