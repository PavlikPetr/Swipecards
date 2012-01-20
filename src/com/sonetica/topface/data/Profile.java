package com.sonetica.topface.data;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;

/*
 * Структура профиля владельца устройства
 */
public class Profile extends AbstractData {
  // Data
  public String first_name;      // имя пользователя
  public int    age;             // возраст пользователя
  public int    sex;             // секс пользователя
  public int    unread_rates;    // количество непрочитанных оценок пользователя
  public int    unread_likes;    // количество непрочитанных “понравилось” пользователя
  public int    unread_messages; // количество непрочитанных сообщений пользователя
  public String photo_url;       // URL аватарки пользователя
  public String city;            // название города пользвоателя
  public int    money;           // количество монет у пользователя
  public int    power;           // количество энергии пользователя
  public int    average_rate;    // средняя оценка текущего пользователя
  //---------------------------------------------------------------------------
  @Override
  public String getLink() {
    return photo_url;
  }
  //---------------------------------------------------------------------------
  public static Profile parse(JSONObject response) {
    Profile profile = new Profile();
    try {
      profile.first_name      = response.getString("first_name");
      profile.age             = response.getInt("age");
      profile.sex             = response.getInt("sex");
      profile.unread_rates    = response.getInt("unread_rates");
      profile.unread_likes    = response.getInt("unread_likes");
      profile.unread_messages = response.getInt("unread_messages");
      profile.photo_url       = response.getString("photo_url");
      profile.city            = response.getString("city");
      profile.money           = response.getInt("money");
      profile.power           = response.getInt("power");
      profile.average_rate    = response.getInt("average_rate");
    } catch(JSONException e) {
      Debug.log(null,"Wrong response parsing: " + e);
    }
    return profile;
  }
  //---------------------------------------------------------------------------
}
