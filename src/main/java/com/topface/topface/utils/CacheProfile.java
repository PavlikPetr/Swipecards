package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.util.SparseArrayCompat;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Gift;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.data.Options;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/* Cache Profile */
public class CacheProfile {
    public static final String ACTION_PROFILE_LOAD = "com.topface.topface.ACTION.PROFILE_LOAD";
    private static AtomicBoolean mIsLoaded = new AtomicBoolean(false);
    // Data
    public static int uid;             // id пользователя в топфейсе
    public static String first_name;   // имя пользователя
    public static int age;             // возраст пользователя
    public static int sex;             // пол пользователя
    public static int unread_likes;    // количество непрочитанных “понравилось” пользователя
    public static int unread_messages; // количество непрочитанных сообщений пользователя
    public static int unread_mutual;   // количество непрочитанных симпатий
    public static int unread_visitors; // количество непрочитанных гостей
    public static int unread_fans;     // количество непрочитаных поклонников
    public static int unread_admirations; // количество непрочитаных восхищений
    public static City city;           // город пользователя
    public static int money;           // количество монет у пользователя
    public static int likes;           // количество симпатий пользователя
    public static DatingFilter dating; //Фильтр поиска

    public static boolean paid; // признак платящего пользоателя
    public static boolean show_ad = true; // флаг показа рекламы

    //Premium
    public static boolean premium;
    public static boolean invisible;

    public final static int NOTIFICATIONS_MESSAGE = 0;
    public final static int NOTIFICATIONS_SYMPATHY = 1;
    public final static int NOTIFICATIONS_LIKES = 2;
    public final static int NOTIFICATIONS_VISITOR = 4;

    public static LinkedList<FormItem> forms;
    protected static String status; // статус пользователя
    public static int background_id;
    public static Photos photos;
    public static Photo photo;

    public static int totalPhotos;

    public static final String PROFILE_CACHE_KEY = "profile_cache";
    public static final String OPTIONS_CACHE_KEY = "options_cache";
    public static final String GP_PRODUCTS_CACHE_KEY = "google_play_products_cache";

    public static ArrayList<Gift> gifts = new ArrayList<Gift>();
    public static SparseArrayCompat<Profile.TopfaceNotifications> notifications;


    public static boolean hasMail;
    public static boolean emailGrabbed;
    public static boolean emailConfirmed;

    public static long profileUpdateTime;
    public static int xstatus;
    private static boolean editor;
    public static boolean canInvite;

    private static void setProfileCache(final ApiResponse response) {
        //Пишем в SharedPreferences в отдельном потоке
        new BackgroundThread() {
            @Override
            public void execute() {
                if (response != null) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
                    editor.putString(PROFILE_CACHE_KEY, response.toJson().toString());
                    editor.commit();
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(PROFILE_CACHE_KEY);
                    editor.remove(OPTIONS_CACHE_KEY);
                    editor.remove(GP_PRODUCTS_CACHE_KEY);
                    editor.commit();
                }
            }
        };
    }

    public static Profile getProfile() {
        Profile profile = new Profile();
        profile.uid = uid;
        profile.firstName = first_name;
        profile.age = age;
        profile.sex = sex;

        profile.notifications = notifications;
        profile.email = hasMail;
        profile.emailConfirmed = emailConfirmed;
        profile.emailGrabbed = emailGrabbed;

        profile.premium = premium;
        profile.invisible = invisible;

        profile.city = city;

        profile.dating = dating;
        profile.forms = forms;
        profile.photos = photos;
        profile.setStatus(status);
        profile.photo = photo;
        profile.gifts = gifts;
        profile.background = background_id;

        profile.photosCount = totalPhotos;

        profile.paid = paid;
        profile.showAd = show_ad;
        profile.xstatus = xstatus;
        profile.setEditor(editor);

        profile.canInvite = canInvite;


        return profile;
    }

    public static void setProfile(Profile profile, ApiResponse response) {
        setProfile(profile, response, ProfileRequest.P_ALL);
    }

    public static void setProfile(Profile profile, ApiResponse response, int part) {

        switch (part) {
            case ProfileRequest.P_NECESSARY_DATA:
                gifts = profile.gifts;
                invisible = profile.invisible;
                premium = profile.premium;
                show_ad = profile.showAd;
                photo = profile.photo;
                photos = profile.photos;
                break;
            case ProfileRequest.P_ALL:
                Editor.init(profile);
                uid = profile.uid;
                first_name = profile.firstName;
                age = profile.age;
                sex = profile.sex;
                city = profile.city;

                notifications = profile.notifications;
                hasMail = profile.email;
                emailConfirmed = profile.emailConfirmed;
                emailGrabbed = profile.emailGrabbed;

                premium = profile.premium;
                invisible = profile.invisible;
                dating = profile.dating;
                forms = profile.forms;

                photos = profile.photos;
                photo = profile.photo;
                status = profile.getStatus();
                gifts = profile.gifts;
                background_id = profile.background;

                totalPhotos = profile.photosCount;

                paid = profile.paid;
                show_ad = profile.showAd;

                xstatus = profile.xstatus;

                canInvite = profile.canInvite;

                editor = profile.isEditor();

                setProfileCache(response);
                break;
        }
        setProfileUpdateTime();
    }

    public static String getStatus() {
        return status;
    }

    public static void setStatus(String status) {
        CacheProfile.status = Profile.normilizeStatus(status);
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
        mIsLoaded.set(true);
        return result;
    }

    /**
     * Опции по умолчанию
     */
    private static Options options;
    private static GooglePlayProducts mProducts;

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
                    options = new Options(new JSONObject(optionsCache));
                } catch (JSONException e) {
                    Debug.error(e);
                }
            }

            if (options == null) {
                //Если по каким то причинам кэша нет и опции нам в данный момент взять негде.
                //то просто используем их по умолчанию
                options = new Options((JSONObject) null);
            }
        }
        return options;
    }

    /**
     * Данные из сервиса googleplay.getProducts
     */
    public static GooglePlayProducts getGooglePlayProducts() {
        if (mProducts == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            String productsCache = preferences.getString(GP_PRODUCTS_CACHE_KEY, null);
            if (productsCache != null) {
                //Получаем опции из кэша
                try {
                    mProducts = new GooglePlayProducts(
                            new JSONObject(productsCache)
                    );
                } catch (JSONException e) {
                    Debug.error(e);
                }
            }

            if (mProducts == null) {
                //Если по каким то причинам кэша нет и опции нам в данный момент взять негде.
                //то просто используем их по умолчанию
                mProducts = new GooglePlayProducts((JSONObject) null);
            }
        }
        return mProducts;
    }

    public static boolean checkIsFillData() {
        return city != null && !city.isEmpty() && age != 0 && first_name != null && photo != null;
    }

    public static void clearProfile() {
        clearOptions();
        setProfile(new Profile(), null);
    }

    public static void clearOptions() {
        options = null;
    }

    private static void setProfileUpdateTime() {
        Calendar calendar = Calendar.getInstance();
        profileUpdateTime = calendar.getTimeInMillis();
    }

    public static boolean isLoaded() {
        return mIsLoaded.get();
    }

    public static boolean isEmpty() {
        return isLoaded() && uid == 0;
    }

    public static void setOptions(Options newOptions, final JSONObject response) {
        options = newOptions;
        //Каждый раз не забываем кешировать запрос опций, но делаем это в отдельном потоке
        if (response != null) {
            new BackgroundThread() {
                @Override
                public void execute() {
                    PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit()
                            .putString(OPTIONS_CACHE_KEY, response.toString())
                            .commit();
                }
            };
        }
    }

    public static void setGooglePlayProducts(GooglePlayProducts products, final JSONObject response) {
        mProducts = products;
        //Каждый раз не забываем кешировать запрос продуктов, но делаем это в отдельном потоке
        if (response != null) {
            new BackgroundThread() {
                @Override
                public void execute() {
                    PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit()
                            .putString(GP_PRODUCTS_CACHE_KEY, response.toString())
                            .commit();
                }
            };
        }
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
        return preferences != null && preferences.getBoolean(Static.PREFERENCES_NEED_CHANGE_PASSWORD, false);
    }

    public static void onPasswordChanged(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_NEED_CHANGE_PASSWORD, false);
        editor.commit();
    }

    public static boolean needCityConfirmation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return preferences != null && preferences.getBoolean(Static.PREFERENCES_NEED_CITY_CONFIRM, false);
    }

    public static void onCityConfirmed(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_NEED_CITY_CONFIRM, false);
        editor.commit();
    }

    public static void onRegistration(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_NEED_CHANGE_PASSWORD, false);
        editor.putBoolean(Static.PREFERENCES_NEED_CITY_CONFIRM, true);
        editor.commit();
    }

    public static boolean isEditor() {
        return editor;
    }
}
