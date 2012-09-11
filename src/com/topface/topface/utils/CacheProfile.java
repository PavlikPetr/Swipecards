package com.topface.topface.utils;

import java.util.HashMap;
import java.util.LinkedList;
import com.topface.topface.data.Profile;
import android.content.Context;
import android.util.SparseArray;

/* Cache Profile */
public class CacheProfile  {
    // Data
    public static int uid; // id пользователя в топфейсе
    public static String first_name; // имя пользователя
    public static int age; // возраст пользователя
    public static int sex; // секс пользователя
    public static int unread_rates; // количество непрочитанных оценок пользователя
    public static int unread_likes; // количество непрочитанных “понравилось” пользователя
    public static int unread_messages; // количество непрочитанных сообщений пользователя
    public static int unread_mutual; // количество непрочитанных симпатий
    // ???
    public static String avatar_big; // аватарка пользователя большого размера
    public static String avatar_small; // аватарки пользователя маленького размера
    // City
    public static int city_id; // идентификтаор города пользователя
    public static String city_name; // название города пользователя
    public static String city_full; // полное название города пользвоателя
    public static int money; // количество монет у пользователя
    public static int power; // количество энергии пользователя
    public static int average_rate; // средняя оценка текущего пользователя
    // Form
//    public static int form_job_id; // идентификатор рабочей партии пользователя
//    public static String form_job; // описание оригинальной работы пользователя
//    public static int form_status_id; // идентификатор предопределенного статуса пользователя
//    public static String form_status; // описание оригинального статуса пользователя
//    public static int form_education_id; // идентификатор предопределенного уровня образования пользователя
//    public static int form_marriage_id; // идентификатор предопределенного семейного положения пользователя
//    public static int form_finances_id; // идентификатор предопределенного финансового положения пользователя
//    public static int form_character_id; // идентификатор предопределенной характеристики пользователя
//    public static int form_smoking_id; // идентификатор предопределенного отношения к курению пользователя
//    public static int form_alcohol_id; // идентификатор предопределенного отношения к алкоголю пользователя
//    public static int form_fitness_id; // идентификатор предопределенного отношения к спорту пользователя
//    public static int form_communication_id; // идентификатор предопределенного отношения к коммуникациям пользователя
//    public static int form_weight; // вес пользователя
//    public static int form_height; // рост пользователя
//    public static int form_hair_id; // идентификатор цвета воло пользователя
//    public static int form_eye_id; // идентификатор цвета глаз пользователя
//    public static int form_children_id; // идентификатор количества детей пользователя
//    public static int form_residence_id; // идентификатор условий проживания пользователя
//    public static int form_car_id; // идентификатор наличия автомобиля у пользователя
//    public static String form_car; // текстовое описание присутствующего автомобиля у пользователя
//    public static String form_first_dating; // текстовое описание свидания пользователя
//    public static String form_achievements; // текстовое описание достижений пользователя
//    //{Array} form_countries; // массив идентификаторов стран, в которых бывал пользователь
//    public static String form_restaurants; // описание предпочитаемых ресторанов пользователя
//    public static String form_valuables; // описание ценностей пользователя
//    public static String form_aspirations; // описание достижений пользователя
    // Dating
    public static int dating_sex; // пол пользователей для поиска
    public static int dating_age_start; // начальный возраст для пользователей
    public static int dating_age_end; // конечный возраст для пользователей
    public static int dating_city_id; // идентификатор города для поиска пользователей
    public static String dating_city_name; // наименование пользователя в русской локали
    public static String dating_city_full; // полное наименование города
    
    public static LinkedList<FormItem> forms;
    public static SparseArray<HashMap<String, String>> photoLinks; // альбом пользователя
    public static String status; // статус пользователя
    public static boolean isNewbie; // поле новичка
	public static int background_res_id;

    public static boolean init(Context context) {
        try {
            //SharedPreferences preferences = context.getSharedPreferences(Global.PROFILE_PREFERENCES_TAG, Context.MODE_PRIVATE);

            //uid        = preferences.getInt(context.getString(R.string.cache_profile_name),0);
            //first_name = preferences.getString(context.getString(R.string.cache_profile_name),"");
            //request.geo    = CacheProfile.filter_geo;
            //request.online = CacheProfile.filter_online;

        } catch(Exception e) {
            Debug.log("CacheProfile", "init exception:" + e);
            return false;
        }
        return true;
    }

    public static void setData(Profile profile) {
        updateAvatars(profile);
        updateCity(profile);
        updateDating(profile);
        updateNotifications(profile);
//        isNewbie = profile.isNewbie;
    }

    public static void updateAvatars(Profile profile) {
//        avatar_big = profile.avatar_big;
//        avatar_small = profile.avatar_small;
    }

    public static void updateCity(Profile profile) {
        city_id = profile.city_id;
        city_name = profile.city_name;
        city_full = profile.city_full;
    }

    public static void updateDating(Profile profile) {
        dating_sex = profile.dating_sex;
        dating_age_start = profile.dating_age_start;
        dating_age_end = profile.dating_age_end;
        dating_city_id = profile.dating_city_id;
        dating_city_name = profile.dating_city_name;
        dating_city_full = profile.dating_city_full;
    }

    public static void updateNotifications(Profile profile) {
        money = profile.money;
        power = profile.power;
        unread_rates = profile.unread_rates;
        unread_likes = profile.unread_likes;
        unread_messages = profile.unread_messages;
        unread_mutual = profile.unread_mutual;
        average_rate = profile.average_rate;
    }

    public static Profile getProfile() {
        Profile profile = new Profile();
        profile.uid = uid;
        profile.first_name = first_name;
        profile.age = age;
        profile.sex = sex;
        profile.money = money;
        profile.power = power;
        profile.unread_rates = unread_rates;
        profile.unread_likes = unread_likes;
        profile.unread_messages = unread_messages;
        profile.unread_mutual = unread_mutual;
        profile.average_rate = average_rate;
        
//        profile.avatar_big = avatar_big;
//        profile.avatar_small = avatar_small;
        
        profile.city_id = city_id;
        profile.city_name = city_name;
        profile.city_full = city_full;
        
        profile.dating_sex = dating_sex;
        profile.dating_age_start = dating_age_start;
        profile.dating_age_end = dating_age_end;
        profile.dating_city_id = dating_city_id;
        profile.dating_city_name = dating_city_name;
        profile.dating_city_full = dating_city_full;
        
        profile.forms = forms;
        
//        profile.form_job_id = form_job_id;
//        profile.form_job = form_job;
//        profile.form_status_id = form_status_id;
//        profile.form_status = form_status;
//        profile.form_education_id = form_education_id;
//        profile.form_marriage_id = form_marriage_id;
//        profile.form_finances_id = form_finances_id;
//        profile.form_character_id = form_character_id;
//        profile.form_smoking_id = form_smoking_id;
//        profile.form_alcohol_id = form_alcohol_id;
//        profile.form_fitness_id = form_fitness_id;
//        profile.form_communication_id = form_communication_id;
//        profile.form_weight = form_weight;
//        profile.form_height = form_height;
//        
//        profile.form_hair_id = form_hair_id;
//        profile.form_eye_id = form_eye_id;
//        profile.form_children_id = form_children_id;
//        profile.form_residence_id = form_residence_id;
//        profile.form_car_id = form_car_id;
//        profile.form_car = form_car;
//        profile.form_first_dating = form_first_dating;
//        profile.form_achievements = form_achievements;
//        //{Array} form_countries; 
//        profile.form_restaurants = form_restaurants;
//        profile.form_valuables = form_valuables;
//        profile.form_aspirations = form_aspirations;
        
        profile.photoLinks = photoLinks;
        profile.status = status;
//        profile.isNewbie = isNewbie;
        return profile;
    }

    public static void setProfile(Profile profile) {
        uid = profile.uid;
        first_name = profile.first_name;
        age = profile.age;
        sex = profile.sex;
        money = profile.money;
        power = profile.power;
        unread_rates = profile.unread_rates;
        unread_likes = profile.unread_likes;
        unread_messages = profile.unread_messages;
        unread_mutual = profile.unread_mutual;
        average_rate = profile.average_rate;
//        avatar_big = profile.avatar_big;
//        avatar_small = profile.avatar_small;
        
        city_id = profile.city_id;
        city_name = profile.city_name;
        city_full = profile.city_full;
        
        dating_sex = profile.dating_sex;
        dating_age_start = profile.dating_age_start;
        dating_age_end = profile.dating_age_end;
        dating_city_id = profile.dating_city_id;
        dating_city_name = profile.dating_city_name;
        dating_city_full = profile.dating_city_full;
        
        forms = profile.forms;
//        form_job_id = profile.form_job_id;
//        form_job = profile.form_job;
//        form_status_id = profile.form_status_id;
//        form_status = profile.form_status;
//        form_education_id = profile.form_education_id;
//        form_marriage_id = profile.form_marriage_id;
//        form_finances_id = profile.form_finances_id;
//        form_character_id = profile.form_character_id;
//        form_smoking_id = profile.form_smoking_id;
//        form_alcohol_id = profile.form_alcohol_id;
//        form_fitness_id = profile.form_fitness_id;
//        form_communication_id = profile.form_communication_id;
//        form_weight = profile.form_weight;
//        form_height = profile.form_height;
//        
//        form_hair_id = profile.form_hair_id;
//        form_eye_id = profile.form_eye_id;
//        form_children_id = profile.form_children_id;
//        form_residence_id = profile.form_residence_id;
//        form_car_id = profile.form_car_id;
//        form_car = profile.form_car;
//        form_first_dating = profile.form_first_dating;
//        form_achievements = profile.form_achievements;
//        //{Array} form_countries; 
//        form_restaurants = profile.form_restaurants;
//        form_valuables = profile.form_valuables;
//        form_aspirations = profile.form_aspirations;
        
        photoLinks = profile.photoLinks;
        status = profile.status;
//        isNewbie = profile.isNewbie;
    }
}
