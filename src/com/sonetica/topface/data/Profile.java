package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

/*
 *  Класс профиля владельца устройства
 */
public class Profile extends AbstractData {
  // Data
  public int uid;             // id пользователя в топфейсе
  public int age;             // возраст пользователя
  public int sex;             // секс пользователя
  public int unread_rates;    // количество непрочитанных оценок пользователя
  public int unread_likes;    // количество непрочитанных “понравилось” пользователя
  public int unread_messages; // количество непрочитанных сообщений пользователя
  public int money;           // количество монет у пользователя
  public int power;           // количество энергии пользователя
  public int average_rate;    // средняя оценка текущего пользователя
  public String first_name;      // имя пользователя
  public String city_name;       // название города пользвоателя
  public String city_id;         // идентификтаор города пользователя
  public String avatar_big;      // аватарка пользователя большого размера
  public String avatar_small;    // аватарки пользователя маленького размера
  public LinkedList<Album> albums;
  // Dating
  public int dating_sex;       // пол пользователей для поиска
  public int dating_age_start; // начальный возраст для пользователей
  public int dating_age_end;   // конечный возраст для пользователей
  public int dating_city_id;   // идентификатор города для поиска пользователей
  public String dating_city;   // наименование пользователя в русской локали
  // Questionary
  public int questionary_job_id;           // идентификатор рабочей партии пользователя
  public String questionary_job;           // описание оригинальной работы пользователя
  public int questionary_status_id;        // идентификатор предопределенного статуса пользователя
  public String questionary_status;        // описание оригинального статуса пользователя
  public int questionary_education_id;     // идентификатор предопределенного уровня образования пользователя
  public int questionary_marriage_id;      // идентификатор предопределенного семейного положения пользователя
  public int questionary_finances_id;      // идентификатор предопределенного финансового положения пользователя
  public int questionary_character_id;     // идентификатор предопределенной характеристики пользователя
  public int questionary_smoking_id;       // идентификатор предопределенного отношения к курению пользователя
  public int questionary_alcohol_id;       // идентификатор предопределенного отношения к алкоголю пользователя
  public int questionary_fitness_id;       // идентификатор предопределенного отношения к спорту пользователя
  public int questionary_communication_id; // идентификатор предопределенного отношения к коммуникациям пользователя
  public int questionary_weight;           // вес пользователя
  public int questionary_height;           // рост пользователя
  //---------------------------------------------------------------------------
  public static Profile parse(Response response,boolean isNotification) {
    Profile profile = new Profile();
    try {
      JSONObject resp = response.mJSONResult;
        profile.unread_rates    = resp.getInt("unread_rates");
        profile.unread_likes    = resp.getInt("unread_likes");
        profile.unread_messages = resp.getInt("unread_messages");
        if(isNotification)  
          return profile;
        profile.first_name      = resp.getString("first_name");
        profile.uid             = resp.getInt("uid");
        profile.age             = resp.getInt("age");
        profile.sex             = resp.getInt("sex");
        profile.money           = resp.getInt("money");
        profile.power           = resp.getInt("power");
        profile.average_rate    = resp.getInt("average_rate");
        profile.city_name       = resp.getString("city");
        profile.city_id         = resp.getString("city_id");
      //avatars
      JSONObject avatars = resp.getJSONObject("avatars");
        profile.avatar_big   = avatars.getString("big");
        profile.avatar_small = avatars.getString("small");
      //albums
      JSONArray albums = resp.getJSONArray("album");
      profile.albums = new LinkedList<Album>();
        if(albums.length()>0)
          for(int i=0;i<albums.length();i++) {
            JSONObject item = albums.getJSONObject(i);
            Album album = new Album();
            album.id    = item.getInt("id");
            album.small = item.getString("small");
            album.big   = item.getString("big");
            if(!item.isNull("ero")) {
              album.ero   = true;
              album.buy   = item.getBoolean("buy");
              album.cost  = item.getInt("cost");
              album.likes = item.getInt("likes");
              album.dislikes = item.getInt("dislikes");
            } else
              album.ero = false;            
            profile.albums.add(album);
          }
      // dating
      JSONObject dating = resp.getJSONObject("dating");
        profile.dating_sex       = dating.getInt("sex");
        profile.dating_age_start = dating.getInt("age_start");
        profile.dating_age_end   = dating.getInt("age_end");
        profile.dating_city_id   = dating.getInt("city_id");
        profile.dating_city      = dating.getString("city");
      // questionary
      JSONObject questionary = resp.getJSONObject("questionary");
        profile.questionary_job_id = questionary.getInt("job_id");
        profile.questionary_job = questionary.getString("job");
        profile.questionary_status_id = questionary.getInt("status_id");
        profile.questionary_status = questionary.getString("status");
        profile.questionary_education_id = questionary.getInt("education_id");
        profile.questionary_marriage_id = questionary.getInt("marriage_id");
        profile.questionary_finances_id = questionary.getInt("finances_id");
        profile.questionary_character_id = questionary.getInt("character_id");
        profile.questionary_smoking_id = questionary.getInt("smoking_id");
        profile.questionary_alcohol_id = questionary.getInt("alcohol_id");
        profile.questionary_fitness_id = questionary.getInt("fitness_id");
        profile.questionary_communication_id = questionary.getInt("communication_id");
        profile.questionary_weight = questionary.getInt("weight");
        profile.questionary_height = questionary.getInt("height");
    } catch(JSONException e) {
      Debug.log("Profile.class","Wrong response parsing: " + e);
    }
    return profile;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return avatar_big;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return avatar_small;
  }
  //---------------------------------------------------------------------------
}
