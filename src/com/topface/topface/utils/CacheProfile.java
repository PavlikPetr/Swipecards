package com.topface.topface.utils;

import com.topface.topface.Global;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import android.content.Context;
import android.content.SharedPreferences;

/*
 *   Cache Profile
 */
public class CacheProfile {
  // Data
  public static int uid;                // id пользователя в топфейсе
  public static String first_name;      // имя пользователя
  public static int age;                // возраст пользователя
  public static int sex;                // секс пользователя
  // notifications
  public static int money;              // количество монет у пользователя
  public static int power;              // количество энергии пользователя
  public static int unread_rates;       // количество непрочитанных оценок пользователя
  public static int unread_likes;       // количество непрочитанных “понравилось” пользователя
  public static int unread_messages;    // количество непрочитанных сообщений пользователя
  public static int unread_symphaties;  // количество непрочитанных симпатий
  public static int average_rate;       // средняя оценка текущего пользователя
  // avatars
  public static String avatar_big;      // аватарка пользователя большого размера
  public static String avatar_small;    // аватарки пользователя маленького размера
  // city
  public static int    city_id;         // идентификтаор города пользователя
  public static String city_name;       // название города пользователя
  public static String city_full;       // полное название города пользвоателя
  // dating filter
  public static int filter_sex;           // пол пользователей для поиска
  public static int filter_age_start;     // начальный возраст для пользователей
  public static int filter_age_end;       // конечный возраст для пользователей
  public static int filter_city_id;       // идентификатор города для поиска пользователей
  public static String  filter_city_name; // наименование пользователя в русской локали
  public static String  filter_city_full; // полное наименование города
  public static boolean filter_geo;       // идентификатор города для поиска пользователей
  public static boolean filter_online;    // идентификатор города для поиска пользователей
  // questionary
  public static int questionary_job_id;           // идентификатор рабочей партии пользователя
  public static String questionary_job;           // описание оригинальной работы пользователя
  public static int questionary_status_id;        // идентификатор предопределенного статуса пользователя
  public static String questionary_status;        // описание оригинального статуса пользователя
  public static int questionary_education_id;     // идентификатор предопределенного уровня образования пользователя
  public static int questionary_marriage_id;      // идентификатор предопределенного семейного положения пользователя
  public static int questionary_finances_id;      // идентификатор предопределенного финансового положения пользователя
  public static int questionary_character_id;     // идентификатор предопределенной характеристики пользователя
  public static int questionary_smoking_id;       // идентификатор предопределенного отношения к курению пользователя
  public static int questionary_alcohol_id;       // идентификатор предопределенного отношения к алкоголю пользователя
  public static int questionary_fitness_id;       // идентификатор предопределенного отношения к спорту пользователя
  public static int questionary_communication_id; // идентификатор предопределенного отношения к коммуникациям пользователя
  public static int questionary_weight;           // вес пользователя
  public static int questionary_height;           // рост пользователя
  //---------------------------------------------------------------------------
  public static boolean init(Context context) {
    try {
      SharedPreferences preferences = context.getSharedPreferences(Global.PROFILE_PREFERENCES_TAG, Context.MODE_PRIVATE);
      
      //uid        = preferences.getInt(context.getString(R.string.cache_profile_name),0);
      first_name = preferences.getString(context.getString(R.string.cache_profile_name),"");
      //request.geo    = CacheProfile.filter_geo;
      //request.online = CacheProfile.filter_online;

    } catch (Exception e) {
      Debug.log("CacheProfile","init exception:" + e);
      return false;
    }
    return true;
  }
  //---------------------------------------------------------------------------
  public static void load(Context context) {

  }
  //---------------------------------------------------------------------------
  public static void save(Context context) {

  }
  //---------------------------------------------------------------------------
  public static void set(Profile profile) {
    uid = profile.uid;
    first_name = profile.first_name;
    age = profile.age;
    sex = profile.sex;
    updateNotifications(profile);
  }
  //---------------------------------------------------------------------------
  public static void updateNotifications(Profile profile) {
    money = profile.money;
    power = profile.power;
    unread_rates = profile.unread_rates;
    unread_likes = profile.unread_likes;
    unread_messages = profile.unread_messages;
    unread_symphaties = profile.unread_symphaties;
    average_rate = profile.unread_rates;
  }
  //---------------------------------------------------------------------------
}
