package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;

public class SearchUser extends AbstractData {
  // Data
  public int uid;                 // идентификатор пользвоателя
  public String first_name;       // имя пользователя
  public int age;                 // возраст пользователя
  public boolean online;          // флаг нахождения пользователя онлайн
  public String status;           // статус пользователя
  public String[]  avatars_big;   // большая аватарка пользователя
  public String[]  avatars_small; // маленькая аватарка пользователя
  
  //public String geo;                 // - геопозиционные данные пользователя
  //public String geo.city;            // - наименование города пользователя
  //public String geo.city_id;         // - идентификатор города пользователя
  //public String geo.distance;        // - дистация до пользователя (всегда NULL)
  //public String geo.coordinates;     // - координаты пользователя
  //public String geo.coordinates.lat; // - широта нахождения пользоавтеля
  //public String geo.coordinates.lng; // - долгота нахождения пользователя
  
  //---------------------------------------------------------------------------
  public static LinkedList<SearchUser> parse(JSONObject response) {
    LinkedList<SearchUser> userList = new LinkedList<SearchUser>();
    try {
      JSONArray array = response.getJSONArray("users");
      if(array.length()>0)
        for(int i=0;i<array.length();i++) {
          JSONObject item = array.getJSONObject(i);
          SearchUser search = new SearchUser();
          search.uid        = item.getInt("uid");
          search.age        = item.getInt("age");
          search.first_name = item.getString("first_name");
          search.online     = item.getBoolean("online");
          search.status     = item.getString("status");
          JSONArray avatars = response.getJSONArray("avatars");
          if(avatars.length()>0) {
            search.avatars_big   = new String[avatars.length()];
            search.avatars_small = new String[avatars.length()];
            for(int n=0;n<avatars.length();n++) {
              JSONObject avatar = avatars.getJSONObject(i);
              search.avatars_big[n]   = avatar.getString("big");
              search.avatars_small[n] = avatar.getString("small");
            }
          }
          userList.add(search);
        }
    } catch(JSONException e) {
      Debug.log(null,"Wrong response parsing: " + e);
    }
    return userList;
  }
  //---------------------------------------------------------------------------
}
