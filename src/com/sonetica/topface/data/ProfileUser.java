package com.sonetica.topface.data;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class ProfileUser extends AbstractData {
  // Data
  public int uid;                    // идентификатор пользователя
  public int age;                    // возраст пользователя
  public int sex;                    // секс пользователя
  public int last_visit;             // таймстамп последнего посещения приложения
  public int city_id;                // идентификатор города пользователя
  public boolean online;             // флаг наличия пользвоателя в онлайне
  public String status;              // статус пользователя
  public String first_name;          // имя пользователя
  public String first_name_translit; // имя пользователя в транслитерации
  public String platform;            // платформа пользователя
  public String avatars_big;         // большая аватарка пользователя
  public String avatars_small;       // маленькая аватарка пользователя
  public String city_name;           // наименование города пользователя
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
  
  // {Null} geo.distance - дистация до пользователя (всегда NULL)
  // {Object} geo.coordinates - координаты пользователя
  // {Object} geo.coordinates.lat - широта нахождения пользоавтеля
  // {Object} geo.coordinates.lng - долгота нахождения пользователя
  //---------------------------------------------------------------------------
  public static ProfileUser parse(int userId,Response response) {    //нужно знать userId
    ProfileUser profile = new ProfileUser();
    try {
      JSONObject item = response.mJSONResult.getJSONObject("profiles");
      item = item.getJSONObject(""+userId);
        profile.uid        = item.getInt("uid");
        profile.age        = item.getInt("age");
        profile.first_name = item.getString("first_name");
        profile.first_name_translit = item.getString("first_name_translit");
        profile.platform   = item.getString("platform");
        profile.last_visit = item.getInt("last_visit");
        profile.online     = item.getBoolean("online");
        profile.status     = item.getString("status");
      JSONObject geo = item.getJSONObject("geo");
        profile.city_name  = geo.getString("city");
        profile.city_id    = geo.getInt("city_id");
      JSONObject avatars = item.getJSONObject("avatars");
        profile.avatars_big   = avatars.getString("big");
        profile.avatars_small = avatars.getString("small");
        //questionary
      JSONObject questionary = item.getJSONObject("questionary");
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
      Debug.log("SearchUser.class","Wrong response parsing: " + e);
    }
    return profile;
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
