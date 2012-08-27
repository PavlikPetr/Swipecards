package com.topface.topface.data;

import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/* Класс чужого профиля */
public class User extends AbstractDataWithPhotos {
    // Data
    public int uid; // идентификатор пользователя
    public String first_name; // имя пользователя
    public String platform; // платформа пользователя
    public int age; // возраст пользователя
    public int sex; // секс пользователя
    public int last_visit; // таймстамп последнего посещения приложения
    public String status; // статус пользователя
    public boolean online; // флаг наличия пользвоателя в онлайне
//    public String first_name_translit; // имя пользователя в транслитерации
//    public String avatars_big; // большая аватарка пользователя
//    public String avatars_small; // маленькая аватарка пользователя
    
//    {Null} geo.distance - дистация до пользователя (всегда NULL)
//    {Object} geo.coordinates - координаты пользователя
//    {Object} geo.coordinates.lat - широта нахождения пользоавтеля
//    {Object} geo.coordinates.lng - долгота нахождения пользователя
    
    // Form
    public int form_job_id; // идентификатор рабочей партии пользователя
    public String form_job; // описание оригинальной работы пользователя
    public int form_status_id; // идентификатор предопределенного статуса пользователя
    public String form_status; // описание оригинального статуса пользователя
    public int form_education_id; // идентификатор предопределенного уровня образования пользователя
    public int form_marriage_id; // идентификатор предопределенного семейного положения пользователя
    public int form_finances_id; // идентификатор предопределенного финансового положения пользователя
    public int form_character_id; // идентификатор предопределенной характеристики пользователя
    public int form_smoking_id; // идентификатор предопределенного отношения к курению пользователя
    public int form_alcohol_id; // идентификатор предопределенного отношения к алкоголю пользователя
    public int form_fitness_id; // идентификатор предопределенного отношения к спорту пользователя
    public int form_communication_id; // идентификатор предопределенного отношения к коммуникациям пользователя
    public int form_weight; // вес пользователя
    public int form_height; // рост пользователя
    public int form_hair_id; // идентификатор цвета воло пользователя
    public int form_eye_id; // идентификатор цвета глаз пользователя
    public int form_children_id; // идентификатор количества детей пользователя
    public int form_residence_id; // идентификатор условий проживания пользователя
    public int form_car_id; // идентификатор наличия автомобиля у пользователя
    public String form_car; // текстовое описание присутствующего автомобиля у пользователя
    public String form_first_dating; // текстовое описание свидания пользователя
    public String form_achievements; // текстовое описание достижений пользователя
    //{Array} form_countries; // массив идентификаторов стран, в которых бывал пользователь
    public String form_restaurants; // описание предпочитаемых ресторанов пользователя
    public String form_valuables; // описание ценностей пользователя
    public String form_aspirations; // описание достижений пользователя

    public int city_id; // идентификатор города пользователя
    public String city_name; // наименование города пользователя
    public String city_full; // полное наименование города пользователя

    public boolean ero; // флаг наличия эротических фотографий
    public boolean mutual; // флаг наличия симпатии к авторизованному пользователю
    public int score; // средний балл оценок пользователя

    public static User parse(int userId, ApiResponse response) { //нужно знать userId
        User profile = new User();

        try {
            JSONObject item = response.mJSONResult.getJSONObject("profiles");
            item = item.getJSONObject("" + userId);
            profile.uid = item.optInt("uid");
            profile.age = item.optInt("age");
            profile.sex = item.optInt("sex");
            profile.first_name = item.optString("first_name");
//            profile.first_name_translit = item.optString("first_name_translit");
            profile.platform = item.optString("platform");
            profile.last_visit = item.optInt("last_visit");
            profile.online = item.optBoolean("online");
            profile.status = item.optString("status");

            /* JSONObject geo = item.getJSONObject("geo");
             * profile.city_name = geo.getString("city");
             * profile.city_id = geo.getInt("city_id"); */

            // city  
            JSONObject city = item.getJSONObject("city");
            profile.city_id = city.optInt("id");
            profile.city_name = city.optString("name");
            profile.city_full = city.optString("full");

//            JSONObject avatars = item.getJSONObject("avatars");
//            profile.avatars_big = avatars.optString("big");
//            profile.avatars_small = avatars.optString("small");

            //form
            JSONObject form = item.optJSONObject("form");
            profile.form_job_id = form.optInt("job_id");
            profile.form_job = form.optString("job");
            profile.form_status_id = form.optInt("status_id");
            profile.form_status = form.optString("status");
            profile.form_education_id = form.optInt("education_id");
            profile.form_marriage_id = form.optInt("marriage_id");
            profile.form_finances_id = form.optInt("finances_id");
            profile.form_character_id = form.optInt("character_id");
            profile.form_smoking_id = form.optInt("smoking_id");
            profile.form_alcohol_id = form.optInt("alcohol_id");
            profile.form_fitness_id = form.optInt("fitness_id");
            profile.form_communication_id = form.optInt("communication_id");
            profile.form_weight = form.optInt("weight");
            profile.form_height = form.optInt("height");
            profile.form_hair_id = form.optInt("hair_id");
            profile.form_eye_id = form.optInt("eye_id");
            profile.form_children_id = form.optInt("children_id");
            profile.form_residence_id = form.optInt("residence_id");
            profile.form_car_id = form.optInt("car_id");
            profile.form_car = form.optString("car");
            profile.form_first_dating = form.optString("first_dating");
            profile.form_achievements = form.optString("achievements");
            //{Array} form_countries; // массив идентификаторов стран, в которых бывал пользователь
            profile.form_restaurants = form.optString("restaurants");
            profile.form_valuables = form.optString("valuables");
            profile.form_aspirations = form.optString("aspirations");
            
            profile.ero = item.optBoolean("ero");
            profile.mutual = item.optBoolean("mutual");
            profile.score = item.optInt("score");
            
            initPhotos(item, profile);
            
        } catch(Exception e) {
            Debug.log("User.class", "Wrong response parsing: " + e);
        }

        return profile;
    }

    public int getUid() {
        return uid;
    };

    @Override
    public String getLargeLink() {
        return null;
    }

    @Override
    public String getSmallLink() {
        return null;
    }
}
