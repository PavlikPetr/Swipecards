package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/*
 *  Класс профиля владельца устройства
 */
public class Profile extends AbstractData implements IAlbumData {
  // Data
  public int uid;                // id пользователя в топфейсе
  public String first_name;      // имя пользователя
  public int age;                // возраст пользователя
  public int sex;                // секс пользователя
  public int unread_rates;       // количество непрочитанных оценок пользователя
  public int unread_likes;       // количество непрочитанных “понравилось” пользователя
  public int unread_messages;    // количество непрочитанных сообщений пользователя
  public int unread_symphaties;  // количество непрочитанных симпатий
  public String avatar_big;      // аватарка пользователя большого размера
  public String avatar_small;    // аватарки пользователя маленького размера
  public int city_id;            // идентификтаор города пользователя
  public String city_name;       // название города пользователя
  public String city_full;       // полное название города пользвоателя
  public int money;              // количество монет у пользователя
  public int power;              // количество энергии пользователя
  public int average_rate;       // средняя оценка текущего пользователя
 
  // Dating filter
  public int filter_sex;           // пол пользователей для поиска
  public int filter_age_start;     // начальный возраст для пользователей
  public int filter_age_end;       // конечный возраст для пользователей
  public int filter_city_id;       // идентификатор города для поиска пользователей
  public String filter_city_name;  // наименование пользователя в русской локали
  public String filter_city_full;  // полное наименование города
  public boolean filter_geo;       // идентификатор города для поиска пользователей
  public boolean filter_online;    // идентификатор города для поиска пользователей
  
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
  
  public LinkedList<Album> albums; // альбом пользователя
  
  public String status;            // статус пользователя
  //---------------------------------------------------------------------------
  public static Profile parse(ApiResponse response) {
    Profile profile = new Profile();
    
    try {
      JSONObject resp = response.mJSONResult;
        profile.unread_rates    = resp.optInt("unread_rates");
        profile.unread_likes    = resp.optInt("unread_likes");
        profile.unread_messages = resp.optInt("unread_messages");
        profile.unread_symphaties = resp.optInt("unread_symphaties");
        profile.average_rate    = resp.optInt("average_rate");
        profile.money = resp.optInt("money");
        profile.power = resp.optInt("power");
        
        profile.uid  = resp.optInt("uid");
        profile.age  = resp.optInt("age");
        profile.sex  = resp.optInt("sex");
        profile.status = resp.optString("status");
        profile.first_name  = resp.optString("first_name");
        
      // city
      if(!resp.isNull("city")) {
        JSONObject city = resp.getJSONObject("city");
        profile.city_id    = city.optInt("id");            
        profile.city_name  = city.optString("name");
        profile.city_full  = city.optString("full");
      }
        
      // avatars
      if(!resp.isNull("avatars")) {
        JSONObject avatars = resp.getJSONObject("avatars");
        profile.avatar_big   = avatars.optString("big");
        profile.avatar_small = avatars.optString("small");
      }
        
      // albums
      if(!resp.isNull("album")) {
        JSONArray albums = resp.getJSONArray("album");
        profile.albums = new LinkedList<Album>();
        if(albums.length()>0)
          for(int i=0;i<albums.length();i++) {
            JSONObject item = albums.getJSONObject(i);
            Album album = new Album();
            album.id    = item.optInt("id");
            album.small = item.optString("small");
            album.big   = item.optString("big");
            if(!item.isNull("ero")) {
              album.ero   = true;
              album.buy   = item.optBoolean("buy");
              album.cost  = item.optInt("cost");
              album.likes = item.optInt("likes");
              album.dislikes = item.optInt("dislikes");
            } else
              album.ero = false;            
            profile.albums.add(album);
          }
      }
        
      // dating filter
      if(!resp.isNull("dating")) {
        JSONObject dating = resp.getJSONObject("dating");
        profile.filter_sex       = dating.optInt("sex");
        profile.filter_age_start = dating.optInt("age_start");
        profile.filter_age_end   = dating.optInt("age_end");
        JSONObject datingCity = dating.getJSONObject("city");
          profile.filter_city_id   = datingCity.optInt("id");            
          profile.filter_city_name = datingCity.optString("name");
          profile.filter_city_full = datingCity.optString("full");
      }
          
      // questionary
      if(!resp.isNull("questionary")) {
        JSONObject questionary = resp.getJSONObject("questionary");
        profile.questionary_job_id = questionary.optInt("job_id");
        profile.questionary_job = questionary.optString("job");
        profile.questionary_status_id = questionary.optInt("status_id");
        profile.questionary_status = questionary.optString("status");
        profile.questionary_education_id = questionary.optInt("education_id");
        profile.questionary_marriage_id = questionary.optInt("marriage_id");
        profile.questionary_finances_id = questionary.optInt("finances_id");
        profile.questionary_character_id = questionary.optInt("character_id");
        profile.questionary_smoking_id = questionary.optInt("smoking_id");
        profile.questionary_alcohol_id = questionary.optInt("alcohol_id");
        profile.questionary_fitness_id = questionary.optInt("fitness_id");
        profile.questionary_communication_id = questionary.optInt("communication_id");
        profile.questionary_weight = questionary.optInt("weight");
        profile.questionary_height = questionary.optInt("height");
      }
    } catch(Exception e) {
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
