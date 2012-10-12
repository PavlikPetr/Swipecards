package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.topface.topface.App;
import com.topface.topface.data.Album;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

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
    public static int city_id;            // идентификтаор города пользователя
    public static String city_name;       // название города пользователя
    public static String city_full;       // полное название города пользвоателя
    // dating
    public static int dating_sex;           // пол пользователей для поиска
    public static int dating_age_start;     // начальный возраст для пользователей
    public static int dating_age_end;       // конечный возраст для пользователей
    public static int dating_city_id;       // идентификатор города для поиска пользователей
    public static String dating_city_name;  // наименование пользователя в русской локали
    public static String dating_city_full;  // полное наименование города
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

    public static LinkedList<Album> albums; // альбом пользователя
    public static String status;            // статус пользователя
    public static boolean isNewbie;         // поле новичка
    public static final String OPTIONS_CACHE_KEY = "options_cache";
    public static final String PROFILE_CACHE_KEY = "profile_cache";

    public static boolean init(Context context) {
        try {
            //SharedPreferences preferences = context.getSharedPreferences(Global.PROFILE_PREFERENCES_TAG, Context.MODE_PRIVATE);

            //uid        = preferences.getInt(context.getString(R.string.cache_profile_name),0);
            //first_name = preferences.getString(context.getString(R.string.cache_profile_name),"");
            //request.geo    = CacheProfile.filter_geo;
            //request.online = CacheProfile.filter_online;

        } catch (Exception e) {
            Debug.log("CacheProfile", "init exception:" + e);
            return false;
        }
        return true;
    }

    public static void load(Context context) {

    }

    public static void save(Context context) {

    }

    public static void setData(Profile profile, ApiResponse response) {
        updateAvatars(profile);
        updateCity(profile);
        updateDating(profile);
        updateNotifications(profile);
        isNewbie = profile.isNewbie;
        setProfileCache(response);
    }

    private static void setProfileCache(ApiResponse response) {
        if (response != null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
            editor.putString(PROFILE_CACHE_KEY, response.toString());
            editor.commit();
        }
    }

    public static void setData(Profile profile) {
        setData(profile, null);
    }

    /**
     * Загружает профиль из кэша
     *
     * @return
     */
    public static boolean loadProfile() {
        boolean result = false;
        if (uid == 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            String profileCache = preferences.getString(PROFILE_CACHE_KEY, null);
            Profile profile = null;
            if (profileCache != null) {
                //Получаем опции из кэша
                try {
                    profile = Profile.parse(
                            new ApiResponse(
                                    new JSONObject(profileCache)
                            )
                    );
                    profile.unread_likes = 0;
                    profile.unread_messages = 0;
                    profile.unread_rates = 0;
                    profile.unread_symphaties = 0;
                    setData(profile);
                    result = true;
                } catch (JSONException e) {
                    Debug.error(e);
                }
            }
        }
        return result;
    }

    public static void updateAvatars(Profile profile) {
        avatar_big = profile.avatar_big;
        avatar_small = profile.avatar_small;
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
        unread_symphaties = profile.unread_symphaties;
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
        profile.unread_symphaties = unread_symphaties;
        profile.average_rate = average_rate;
        profile.avatar_big = avatar_big;
        profile.avatar_small = avatar_small;
        profile.city_id = city_id;
        profile.city_name = city_name;
        profile.city_full = city_full;
        profile.dating_sex = dating_sex;
        profile.dating_age_start = dating_age_start;
        profile.dating_age_end = dating_age_end;
        profile.dating_city_id = dating_city_id;
        profile.dating_city_name = dating_city_name;
        profile.dating_city_full = dating_city_full;
        profile.questionary_job_id = questionary_job_id;
        profile.questionary_job = questionary_job;
        profile.questionary_status_id = questionary_status_id;
        profile.questionary_status = questionary_status;
        profile.questionary_education_id = questionary_education_id;
        profile.questionary_marriage_id = questionary_marriage_id;
        profile.questionary_finances_id = questionary_finances_id;
        profile.questionary_character_id = questionary_character_id;
        profile.questionary_smoking_id = questionary_smoking_id;
        profile.questionary_alcohol_id = questionary_alcohol_id;
        profile.questionary_fitness_id = questionary_fitness_id;
        profile.questionary_communication_id = questionary_communication_id;
        profile.questionary_weight = questionary_weight;
        profile.questionary_height = questionary_height;
        profile.albums = albums;
        profile.status = status;
        profile.isNewbie = isNewbie;
        return profile;
    }

    public static void setProfile(Profile profile, ApiResponse response) {
        uid = profile.uid;
        first_name = profile.first_name;
        age = profile.age;
        sex = profile.sex;
        money = profile.money;
        power = profile.power;
        unread_rates = profile.unread_rates;
        unread_likes = profile.unread_likes;
        unread_messages = profile.unread_messages;
        unread_symphaties = profile.unread_symphaties;
        average_rate = profile.average_rate;
        avatar_big = profile.avatar_big;
        avatar_small = profile.avatar_small;
        city_id = profile.city_id;
        city_name = profile.city_name;
        city_full = profile.city_full;
        dating_sex = profile.dating_sex;
        dating_age_start = profile.dating_age_start;
        dating_age_end = profile.dating_age_end;
        dating_city_id = profile.dating_city_id;
        dating_city_name = profile.dating_city_name;
        dating_city_full = profile.dating_city_full;
        questionary_job_id = profile.questionary_job_id;
        questionary_job = profile.questionary_job;
        questionary_status_id = profile.questionary_status_id;
        questionary_status = profile.questionary_status;
        questionary_education_id = profile.questionary_education_id;
        questionary_marriage_id = profile.questionary_marriage_id;
        questionary_finances_id = profile.questionary_finances_id;
        questionary_character_id = profile.questionary_character_id;
        questionary_smoking_id = profile.questionary_smoking_id;
        questionary_alcohol_id = profile.questionary_alcohol_id;
        questionary_fitness_id = profile.questionary_fitness_id;
        questionary_communication_id = profile.questionary_communication_id;
        questionary_weight = profile.questionary_weight;
        questionary_height = profile.questionary_height;
        albums = profile.albums;
        status = profile.status;
        isNewbie = profile.isNewbie;
        setProfileCache(response);
    }

    /**
     * Опции по умолчанию
     */
    private static Options options;

    /**
     * Данные из сервиса options
     *
     * @return
     */
    public static Options getOptions() {
        if (options == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            String optionsCache = preferences.getString(OPTIONS_CACHE_KEY, null);
            if (optionsCache != null) {
                //Получаем опции из кэша
                try {
                    options = Options.parse(
                            new ApiResponse(
                                    new JSONObject(optionsCache)
                            )
                    );
                } catch (JSONException e) {
                    Debug.error(e);
                }
            }

            if (options == null) {
                //Если по каким то причинам кэша нет и опции нам в данный момент взять негде.
                //то просто используем их по умолчанию
                options = new Options();
            }
        }
        return options;
    }

    public static boolean isOptionsLoaded() {
        return options != null;
    }

    public static void setOptions(Options newOptions, JSONObject response) {
        options = newOptions;
        //Каждый раз не забываем кешировать запрос опций
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
        editor.putString(OPTIONS_CACHE_KEY, response.toString());
        editor.commit();
    }
}
