package com.topface.topface.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.topface.topface.App;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/* Cache Profile */
public class CacheProfile {
    // Data
    public static int uid;             // id пользователя в топфейсе
    public static String first_name;   // имя пользователя
    public static int age;             // возраст пользователя
    public static int sex;             // секс пользователя
    public static int unread_rates;    // количество непрочитанных оценок пользователя
    public static int unread_likes;    // количество непрочитанных “понравилось” пользователя
    public static int unread_messages; // количество непрочитанных сообщений пользователя
    public static int unread_mutual;   // количество непрочитанных симпатий

    // City
    public static int city_id;      // идентификтаор города пользователя
    public static String city_name; // название города пользователя
    public static String city_full; // полное название города пользвоателя
    public static int money;        // количество монет у пользователя
    public static int power;        // количество энергии пользователя
    public static int average_rate; // средняя оценка текущего пользователя
    // Dating
    public static int dating_sex; // пол пользователей для поиска
    public static int dating_age_start; // начальный возраст для пользователей
    public static int dating_age_end; // конечный возраст для пользователей
    public static int dating_city_id; // идентификатор города для поиска пользователей
    public static String dating_city_name; // наименование пользователя в русской локали
    public static String dating_city_full; // полное наименование города

    public static LinkedList<FormItem> forms;
    public static String status; // статус пользователя
    public static boolean isNewbie; // поле новичка
    public static int background_id;
    public static Photos photos;
    public static Photo photo;
    public static final String PROFILE_CACHE_KEY = "profile_cache";

    public static LinkedList<Gift> gifts = new LinkedList<Gift>();

    public static void setData(Profile profile) {
        updateCity(profile);
        updateDating(profile);
        updateNotifications(profile);
        gifts = profile.gifts;
    }

    private static void setProfileCache(ApiResponse response) {
        if (response != null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
            editor.putString(PROFILE_CACHE_KEY, response.toString());
            editor.commit();
        }
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
        profile.photos = photos;
        profile.status = status;
        profile.photo = photo;
        profile.gifts = gifts;
        profile.background = background_id;
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
        unread_mutual = profile.unread_mutual;
        average_rate = profile.average_rate;

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

        photos = profile.photos;
        photo = profile.photo;
        status = profile.status;
        gifts = profile.gifts;
        background_id = profile.background;

        setProfileCache(response);
    }

    /**
     * Загружает профиль из кэша
     *
     * @return profile loaded
     */
    public static boolean loadProfile() {
        boolean result = false;
        if (uid == 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            String profileCache = preferences.getString(PROFILE_CACHE_KEY, null);
            Profile profile;
            if (profileCache != null) {
                //Получаем опции из кэша
                try {
                    ApiResponse response = new ApiResponse(
                            new JSONObject(profileCache)
                    );
                    profile = Profile.parse(response);
                    profile.unread_likes = 0;
                    profile.unread_messages = 0;
                    profile.unread_rates = 0;
                    profile.unread_mutual = 0;
                    setProfile(profile, response);
                    result = true;
                } catch (JSONException e) {
                    Debug.error(e);
                }
            }
        }
        return result;
    }
}
