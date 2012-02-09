package com.sonetica.topface.data;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

/*
 *  Класс профиля владельца устройства
 */
public class Profile extends AbstractData {
  // Data
  public int    age;             // возраст пользователя
  public int    sex;             // секс пользователя
  public int    unread_rates;    // количество непрочитанных оценок пользователя
  public int    unread_likes;    // количество непрочитанных “понравилось” пользователя
  public int    unread_messages; // количество непрочитанных сообщений пользователя
  public int    money;           // количество монет у пользователя
  public int    power;           // количество энергии пользователя
  public int    average_rate;    // средняя оценка текущего пользователя
  public String first_name;      // имя пользователя
  public String photo_url;       // URL аватарки пользователя
  public String city_name;       // название города пользвоателя
  public String city_id;         // идентификтаор города пользователя
  //---------------------------------------------------------------------------
  public static Profile parse(Response response,boolean isNotification) {
    Profile profile = new Profile();
    try {
      JSONObject resp = response.mJSONResult;
      if(isNotification) {
        profile.unread_rates    = resp.getInt("unread_rates");
        profile.unread_likes    = resp.getInt("unread_likes");
        profile.unread_messages = resp.getInt("unread_messages");
        return profile;
      }
      profile.first_name      = resp.getString("first_name");
      profile.age             = resp.getInt("age");
      profile.sex             = resp.getInt("sex");
      profile.unread_rates    = resp.getInt("unread_rates");
      profile.unread_likes    = resp.getInt("unread_likes");
      profile.unread_messages = resp.getInt("unread_messages");
      profile.photo_url       = resp.getString("photo_url");
      profile.city_name       = resp.getString("city");
      profile.city_id         = resp.getString("city_id");
      profile.money           = resp.getInt("money");
      profile.power           = resp.getInt("power");
      profile.average_rate    = resp.getInt("average_rate");
    } catch(JSONException e) {
      Debug.log("Profile.class","Wrong response parsing: " + e);
    }
    return profile;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return photo_url;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return photo_url;
  }
  //---------------------------------------------------------------------------
}
