package com.sonetica.topface.data;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class ProfileUser extends AbstractData {
  // Data
  public int      uid;                 // идентификатор пользователя
  public String   first_name;          // имя пользователя
  public String   first_name_translit; // имя пользователя в транслитерации
  public String   platform;            // платформа пользователя
  public int      age;                 // возраст пользователя
  public int      sex;                 // секс пользователя
  public int      last_visit;          // таймстамп последнего посещения приложения
  public String   status;              // статус пользователя
  public boolean  online;              // флаг наличия пользвоателя в онлайне
  public String   avatars_big;         // большая аватарка пользователя
  public String   avatars_small;       // маленькая аватарка пользователя
  public String   city_name;           // наименование города пользователя
  public int      city_id;             // идентификатор города пользователя
    
  // {Null} geo.distance - дистация до пользователя (всегда NULL)
  // {Object} geo.coordinates - координаты пользователя
  // {Object} geo.coordinates.lat - широта нахождения пользоавтеля
  // {Object} geo.coordinates.lng - долгота нахождения пользователя
  //---------------------------------------------------------------------------
  public static LinkedList<ProfileUser> parse(int userId,Response response) {
    LinkedList<ProfileUser> userList = new LinkedList<ProfileUser>();
    try {
      //JSONArray array = response.mJSONResult.getJSONArray("profiles");
      //if(array.length()>0)                          // ЗАПРОС НЕ ОБРАБАТЫВАЕТСЯ КАК МАССИВ
        //for(int i=0;i<array.length();i++) {         // ЕБАННАЯ ХУЙНЯ В ОТВЕТЕ ,  
          //JSONObject item = array.getJSONObject(i);
          JSONObject item = response.mJSONResult.getJSONObject("profiles"); // не массив
          item = item.getJSONObject(""+userId);          //нужно знать userId
          ProfileUser profile = new ProfileUser();
            profile.uid        = item.getInt("uid");
            profile.age        = item.getInt("age");
            profile.first_name = item.getString("first_name");
            profile.first_name_translit = item.getString("first_name_translit");
            profile.platform   = item.getString("platform");
            profile.last_visit = item.getInt("last_visit");
            profile.online     = item.getBoolean("online");
            profile.status     = item.getString("status");
          JSONObject geo = item.getJSONObject("geo");
            profile.city_name = geo.getString("city");
            profile.city_id   = geo.getInt("city_id");
          JSONObject avatars = item.getJSONObject("avatars");
            profile.avatars_big   = avatars.getString("big");
            profile.avatars_small = avatars.getString("small");
          userList.add(profile);
        //}
    } catch(JSONException e) {
      Debug.log("SearchUser.class","Wrong response parsing: " + e);
    }
    return userList;
  }
  //---------------------------------------------------------------------------
}
