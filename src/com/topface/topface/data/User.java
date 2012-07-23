package com.topface.topface.data;

import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/* Класс чужого профиля */
public class User extends AbstractData {
    // Data
    public int uid; // идентификатор пользователя
    public String first_name; // имя пользователя
    public String platform; // платформа пользователя
    public int age; // возраст пользователя
    public int sex; // секс пользователя
    public int last_visit; // таймстамп последнего посещения приложения
    public String status; // статус пользователя
    public boolean online; // флаг наличия пользвоателя в онлайне
    public String first_name_translit; // имя пользователя в транслитерации
    public String avatars_big; // большая аватарка пользователя
    public String avatars_small; // маленькая аватарка пользователя
    // Questionary
    public int questionary_job_id; // идентификатор рабочей партии пользователя
    public String questionary_job; // описание оригинальной работы пользователя
    public int questionary_status_id; // идентификатор предопределенного статуса пользователя
    public String questionary_status; // описание оригинального статуса пользователя
    public int questionary_education_id; // идентификатор предопределенного уровня образования пользователя
    public int questionary_marriage_id; // идентификатор предопределенного семейного положения пользователя
    public int questionary_finances_id; // идентификатор предопределенного финансового положения пользователя
    public int questionary_character_id; // идентификатор предопределенной характеристики пользователя
    public int questionary_smoking_id; // идентификатор предопределенного отношения к курению пользователя
    public int questionary_alcohol_id; // идентификатор предопределенного отношения к алкоголю пользователя
    public int questionary_fitness_id; // идентификатор предопределенного отношения к спорту пользователя
    public int questionary_communication_id; // идентификатор предопределенного отношения к коммуникациям пользователя
    public int questionary_weight; // вес пользователя
    public int questionary_height; // рост пользователя

    public int city_id; // идентификатор города пользователя
    public String city_name; // наименование города пользователя
    public String city_full; // полное наименование города пользователя

    public boolean ero; // флаг наличия эротических фотографий
    public boolean mutual; // флаг наличия симпатии к авторизованному пользователю

    // {Null} geo.distance - дистация до пользователя (всегда NULL)
    // {Object} geo.coordinates - координаты пользователя
    // {Object} geo.coordinates.lat - широта нахождения пользоавтеля
    // {Object} geo.coordinates.lng - долгота нахождения пользователя
    //---------------------------------------------------------------------------
    public static User parse(int userId,ApiResponse response) { //нужно знать userId
        User profile = new User();

        try {
            JSONObject item = response.mJSONResult.getJSONObject("profiles");
            item = item.getJSONObject("" + userId);
            profile.uid = item.optInt("uid");
            profile.age = item.optInt("age");
        profile.sex        = item.optInt("sex");
            profile.first_name = item.optString("first_name");
            profile.first_name_translit = item.optString("first_name_translit");
            profile.platform = item.optString("platform");
            profile.last_visit = item.optInt("last_visit");
            profile.online = item.optBoolean("online");
            profile.status = item.optString("status");
            profile.ero = item.optBoolean("ero");
            profile.mutual = item.optBoolean("mutual");

            /* JSONObject geo = item.getJSONObject("geo");
             * profile.city_name = geo.getString("city");
             * profile.city_id = geo.getInt("city_id"); */

            // city  
            JSONObject city = item.getJSONObject("city");
            profile.city_id = city.optInt("id");
            profile.city_name = city.optString("name");
            profile.city_full = city.optString("full");

            JSONObject avatars = item.getJSONObject("avatars");
            profile.avatars_big = avatars.optString("big");
            profile.avatars_small = avatars.optString("small");

            //questionary
            JSONObject questionary = item.optJSONObject("questionary");
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
        } catch(Exception e) {
            Debug.log("User.class", "Wrong response parsing: " + e);
        }

        return profile;
    }
    //---------------------------------------------------------------------------
    public int getUid() {
        return uid;
    };
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
