package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

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
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.config.ProfileConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/* Cache Profile */
public class CacheProfile {
    public static final String ACTION_PROFILE_LOAD = "com.topface.topface.ACTION.PROFILE_LOAD";
    /**
     * Use sendProfileUpdateBroadcast() static method
     */
    public static final String PROFILE_UPDATE_ACTION = "com.topface.topface.UPDATE_PROFILE";

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

    public static boolean NEED_SHOW_BONUS_COUNTER = false;

    public static LinkedList<FormItem> forms;
    protected static String status; // статус пользователя
    public static int background_id;
    public static Photos photos;
    public static Photo photo;

    public static int totalPhotos;

    public static final String OPTIONS_CACHE_KEY = "options_cache";
    public static final String GP_PRODUCTS_CACHE_KEY = "google_play_products_cache";

    public static ArrayList<Gift> gifts = new ArrayList<>();
    public static SparseArrayCompat<Profile.TopfaceNotifications> notifications;


    public static boolean hasMail;
    public static boolean emailGrabbed;
    public static boolean emailConfirmed;

    public static long profileUpdateTime;
    public static int xstatus;
    private static boolean editor;
    public static boolean canInvite;

    private static void setProfileCache(final ApiResponse response) {
        ProfileConfig config = App.getConfig().getProfileConfig();
        if (response != null) {
            config.setProfileData(response.toJson().toString());
            config.saveConfig();
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(GP_PRODUCTS_CACHE_KEY);
            editor.commit();
        }
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
            ProfileConfig config = App.getConfig().getProfileConfig();
            String profileCache = config.getProfileData();
            Profile profile;
            if (!TextUtils.isEmpty(profileCache)) {
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
                    config.resetAndSaveConfig();
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
            ProfileConfig config = App.getConfig().getProfileConfig();
            String optionsCache = config.getOptionsData();
            if (!TextUtils.isEmpty(optionsCache)) {
                //Получаем опции из кэша, причем передаем флаг, что бы эти опции не кешировались повторно
                try {
                    options = new Options(new JSONObject(optionsCache), false);
                } catch (JSONException e) {
                    //Если произошла ошибка при парсинге кэша, то скидываем опции
                    config.resetOptionsData();
                    Debug.error(e);
                }
            }
            if (options == null) {
                //Если по каким то причинам кэша нет и опции нам в данный момент взять негде.
                //то просто используем их по умолчанию
                options = new Options(null, false);
            }
        }
        return options;
    }

    /**
     * Данные из сервиса googleplay.getProducts
     */
    public static GooglePlayProducts getGooglePlayProducts() {
        if (mProducts == null) {
            String productsCache = PreferenceManager.getDefaultSharedPreferences(App.getContext())
                    .getString(GP_PRODUCTS_CACHE_KEY, null);
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

    public static boolean isDataFilled() {
        return city != null && !city.isEmpty() && age != 0 && first_name != null && photo != null;
    }

    /**
     * Clears CacheProfile fields (does not affect cached data from ProfileConfig)
     */
    public static void clearProfileAndOptions() {
        clearOptions();
        setProfile(new Profile(), null);
    }

    public static void clearOptions() {
        options = null;
    }

    private static void setProfileUpdateTime() {
        profileUpdateTime = System.currentTimeMillis();
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
            ProfileConfig config = App.getConfig().getProfileConfig();
            config.setOptionsData(response.toString());
            config.saveConfig();
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
            LocalBroadcastManager.getInstance(App.getContext())
                    .sendBroadcast(new Intent(GooglePlayProducts.INTENT_UPDATE_PRODUCTS));

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

    public static int getUnreadCounterByFragmentId(BaseFragment.FragmentId id) {
        switch (id) {
            case F_LIKES:
            case F_LIKES_CLOSINGS:
                return CacheProfile.unread_likes;
            case F_MUTUAL:
            case F_MUTUAL_CLOSINGS:
                return CacheProfile.unread_mutual;
            case F_DIALOGS:
                return CacheProfile.unread_messages;
            case F_VISITORS:
                return CacheProfile.unread_visitors;
            case F_FANS:
                return CacheProfile.unread_fans;
            case F_ADMIRATIONS:
                return CacheProfile.unread_admirations;
            case F_BONUS:
                return NEED_SHOW_BONUS_COUNTER ? getOptions().bonus.counter : 0;
            default:
                return 0;
        }
    }

    public static void sendUpdateProfileBroadcast() {
        LocalBroadcastManager.getInstance(App.getContext())
                .sendBroadcast(new Intent(PROFILE_UPDATE_ACTION));
    }
}
