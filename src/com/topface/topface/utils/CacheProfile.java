package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.requests.ApiResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

/* Cache Profile */
public class CacheProfile {
    // Data
    public static int uid;             // id пользователя в топфейсе
    public static String first_name;   // имя пользователя
    public static int age;             // возраст пользователя
    public static int sex;             // пол пользователя
    public static int unread_likes;    // количество непрочитанных “понравилось” пользователя
    public static int unread_messages; // количество непрочитанных сообщений пользователя
    public static int unread_mutual;   // количество непрочитанных симпатий
    public static int unread_visitors; // количество непрочитанных гостей
    public static City city;           // город пользователя
    public static int money;           // количество монет у пользователя
    public static int likes;           // количество симпатий пользователя
    public static int average_rate;    // средняя оценка текущего пользователя
    public static DatingFilter dating; //Фильтр поиска

    public static boolean paid; // признак платящего пользоателя

    //Premium
    public static boolean premium;
    public static boolean invisible;

    public final static int NOTIFICATIONS_MESSAGE = 0;
    public final static int NOTIFICATIONS_SYMPATHY = 1;
    public final static int NOTIFICATIONS_LIKES = 2;
    public final static int NOTIFICATIONS_VISITOR = 4;

    public static LinkedList<FormItem> forms;
    public static String status; // статус пользователя    
    public static int background_id;
    public static Photos photos;
    public static Photo photo;

    public static int totalPhotos;

    public static final String PROFILE_CACHE_KEY = "profile_cache";
    public static final String OPTIONS_CACHE_KEY = "options_cache";

    public static ArrayList<Gift> gifts = new ArrayList<Gift>();
    public static HashMap<Integer, Profile.TopfaceNotifications> notifications;

    public static boolean hasMail;
    public static boolean emailGrabbed;
    public static boolean emailConfirmed;

    public static long profileUpdateTime;

    private static void setProfileCache(final ApiResponse response) {
        //Пишем в SharedPreferences в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (response != null) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
                    editor.putString(PROFILE_CACHE_KEY, response.toJson().toString());
                    editor.commit();
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(PROFILE_CACHE_KEY);
                    editor.remove(OPTIONS_CACHE_KEY);
                    editor.commit();
                }
            }
        }).start();
    }

    public static Profile getProfile() {
        Profile profile = new Profile();
        profile.uid = uid;
        profile.first_name = first_name;
        profile.age = age;
        profile.sex = sex;

        profile.money = money;
        profile.likes = likes;

        profile.average_rate = average_rate;

        profile.notifications = notifications;
        profile.hasMail = hasMail;
        profile.email_confirmed = emailConfirmed;
        profile.email_grabbed = emailGrabbed;

        profile.premium = premium;
        profile.invisible = invisible;

        profile.city = city;

        profile.dating = dating;
        profile.forms = forms;
        profile.photos = photos;
        profile.status = status;
        profile.photo = photo;
        profile.gifts = gifts;
        profile.background = background_id;

        profile.totalPhotos = totalPhotos;

        profile.paid = paid;
        return profile;
    }

    public static void setProfile(Profile profile, ApiResponse response) {
        uid = profile.uid;
        first_name = profile.first_name;
        age = profile.age;
        sex = profile.sex;
        city = profile.city;

        money = profile.money;
        likes = profile.likes;

        average_rate = profile.average_rate;

        notifications = profile.notifications;
        hasMail = profile.hasMail;
        emailConfirmed = profile.email_confirmed;
        emailGrabbed = profile.email_grabbed;

        premium = profile.premium;
        invisible = profile.invisible;
        dating = profile.dating;
        forms = profile.forms;

        photos = profile.photos;
        photo = profile.photo;
        status = profile.status;
        gifts = profile.gifts;
        background_id = profile.background;

        totalPhotos = profile.totalPhotos;

        paid = profile.paid;

        setProfileCache(response);
        setProfileUpdateTime();
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
                    setProfile(profile, response);
                    result = true;
                } catch (JSONException e) {
                    Debug.error(e);
                    //Если произошла ошибка, то чистим кэш, т.к. ошибка связана скорее всего с ним
                    PreferenceManager.getDefaultSharedPreferences(App.getContext())
                            .edit()
                            .remove(PROFILE_CACHE_KEY)
                            .commit();
                }
            }
        }
        return result;
    }

    /**
     * Опции по умолчанию
     */
    private static Options options;

    /**
     * Данные из сервиса options
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

    public static void clearProfile() {
        options = null;
        setProfile(new Profile(), null);
    }

    private static void setProfileUpdateTime() {
        Calendar calendar = Calendar.getInstance();
        profileUpdateTime = calendar.getTimeInMillis();
    }

    public static boolean isLoaded() {
        return uid > 0;
    }

    public static void setOptions(Options newOptions, final JSONObject response) {
        options = newOptions;
        //Каждый раз не забываем кешировать запрос опций, но делаем это в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
                editor.putString(OPTIONS_CACHE_KEY, response.toString());
                editor.commit();
            }
        }).start();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String getUserNameAgeString() {
        return CacheProfile.first_name +
                (CacheProfile.isAgeOk(CacheProfile.age) ? ", " + CacheProfile.age : "");
    }

    private static boolean isAgeOk(int age) {
        return age > 0;
    }

    public static boolean wasCityAsked = false;
    public static boolean wasAvatarAsked = false;

    public static boolean needToChangePassword(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return preferences != null && preferences.getBoolean(Static.PREFERENCES_TAG_NEED_CHANGE_PASSWORD, false);
    }

    public static void onPasswordChanged(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_TAG_NEED_CHANGE_PASSWORD, false);
        editor.commit();
    }

    public static boolean needCityConfirmation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return preferences != null && preferences.getBoolean(Static.PREFERENCES_TAG_NEED_CITY_CONFIRM, false);
    }

    public static void onCityConfirmed(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_TAG_NEED_CITY_CONFIRM, false);
        editor.commit();
    }

    public static void onRegistration(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_TAG_NEED_CHANGE_PASSWORD, true);
        editor.putBoolean(Static.PREFERENCES_TAG_NEED_CITY_CONFIRM, true);
        editor.commit();
    }
}
