package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class FeedLike extends AbstractData implements IAlbumData {
  // Data
  public static int unread_count; // количество оставшихся непрочитанных
  public int id;                  // идентификатор сообщения 
  public int uid;                 // идентификатор отправителя
  public int age;                 // возраст пользователя
  public int city_id;             // идентификатор города отправителя оценки
  public boolean online;          // флаг нахождения пользователя в онлайне
  public boolean unread;          // флаг прочитанного лайка
  public String city_name;        // название города пользователя
  public String city_full;        // полное название города пользвоателя
  public String first_name;       // имя пользователя
  public String avatars_big;      // большая аватарка пользователя
  public String avatars_small;    // маленькая аватарка пользователя
  public int rate;                // значение “понравилось”
  public long created;            // таймштамп отправления “понравилось”
  //---------------------------------------------------------------------------
  public static LinkedList<FeedLike> parse(ApiResponse response) {
    LinkedList<FeedLike> likesList = new LinkedList<FeedLike>();
    
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0)
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          FeedLike.unread_count = response.mJSONResult.getInt("unread");
          FeedLike like = new FeedLike();
            like.first_name  = item.optString("first_name");
            like.id          = item.optInt("id");
            like.uid         = item.optInt("uid");
            like.age         = item.optInt("age");
            like.online      = item.optBoolean("online");
            like.unread      = item.optBoolean("unread");
            like.rate        = item.optInt("rate");
            like.created     = item.optLong("created");
            
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
      Debug.log("FeedLike.class","Wrong response parsing: " + e);
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
