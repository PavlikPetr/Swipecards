package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/*
 * Класс профиля окна топов
 */
public class FeedSymphaty extends AbstractData {
  // Data
  public static int unread_count; // количество оставшихся непрочитанных
  public int id;                // идентификатор симпатии в ленте
  public int uid;               // идентификатор отправителя
  public String first_name;     // имя отправителя в текущей локали
  public int age;               // возраст отправителя
  public int city_id;           // идентификатор города
  public String city_name;      // наименование города в локали указанной при авторизации
  public String city_full;      // полное наименование города с указанием региона, если он определен. Отдается в локали пользователя, указанной при авторизации
  public boolean online;        // флаг нахождения отправителя онлайн
  public boolean unread;        // флаг прочитанной симпатии
  public String avatars_big;    // фото большого размера
  public String avatars_small;  // фото маленького размера
  public long created;          // таймстамп отправления симпатии
  //---------------------------------------------------------------------------
  public static LinkedList<FeedSymphaty> parse(ApiResponse response) {
    LinkedList<FeedSymphaty> symphatyList = new LinkedList<FeedSymphaty>();
    
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("feed");
      if(arr.length()>0) 
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          FeedSymphaty.unread_count = response.mJSONResult.getInt("unread");
          FeedSymphaty symphaty = new FeedSymphaty();
            symphaty.id         = item.optInt("id");
            symphaty.uid        = item.optInt("uid");
            symphaty.first_name = item.optString("first_name");
            symphaty.age        = item.optInt("age");
            symphaty.online     = item.optBoolean("online");
            symphaty.unread     = item.optBoolean("unread");
            symphaty.created    = item.optLong("created");
            
            // city  
            JSONObject city = item.getJSONObject("city");
              symphaty.city_id    = city.optInt("id");            
              symphaty.city_name  = city.optString("name");
              symphaty.city_full  = city.optString("full");
              
            // avatars
            JSONObject avatar  = item.getJSONObject("avatars");
              symphaty.avatars_big   = avatar.optString("big");
              symphaty.avatars_small = avatar.optString("small");
                
          symphatyList.add(symphaty);
        }
    } catch(Exception e) {
      Debug.log("FeedSymphaty.class","Wrong response parsing: " + e);
    }
    
    return symphatyList;
  }
  //---------------------------------------------------------------------------
}
