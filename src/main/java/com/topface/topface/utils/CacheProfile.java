package com.topface.topface.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Products;
import com.topface.topface.data.ProductsDetails;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.ui.fragments.OwnAvatarFragment;
import com.topface.topface.utils.config.SessionConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

/* Cache Profile */
public class CacheProfile {

    @Inject
    TopfaceAppState state;

    public static final String ACTION_PROFILE_LOAD = "com.topface.topface.ACTION.PROFILE_LOAD";
    /**
     * Use sendProfileUpdateBroadcast() static method
     */
    public static final String PROFILE_UPDATE_ACTION = "com.topface.topface.UPDATE_PROFILE";

    public final static int NOTIFICATIONS_MESSAGE = 0;
    public final static int NOTIFICATIONS_SYMPATHY = 1;
    public final static int NOTIFICATIONS_LIKES = 2;
    public final static int NOTIFICATIONS_VISITOR = 4;

    // Data
    public static CountersData countersData;
    public static int uid;                      // id пользователя в топфейсе
    public static String first_name;            // имя пользователя
    public static int age;                      // возраст пользователя
    public static int sex;                      // пол пользователя
    public static City city;                    // город пользователя
    public static int money;                    // количество монет у пользователя
    public static int likes;                    // количество симпатий пользователя
    public static DatingFilter dating;          // Фильтр поиска
    public static boolean paid;                 // признак платящего пользоателя
    public static boolean show_ad = true;       // флаг показа рекламы
    public static boolean premium;              // показывает есть ли у пользователя Vip статус
    public static boolean invisible;            // показывает включен ли режим невидимки
    public static LinkedList<FormItem> forms;   // анкета пользователя
    public static int background_id;            // идентификатор фона в профиле
    public static Photos photos;                // список первых 30 фото
    public static Photo photo;                  // аватарка пользователя
    public static int totalPhotos;              // общее количество фотографий пользователя
    public static boolean email;                // присутсвует ли email
    public static boolean emailGrabbed;         // был ли email введен пользователем
    public static boolean emailConfirmed;       // подтвержден ли email
    public static int xstatus;                  // код цели знакомства пользователя, возможные варианты
    private static boolean editor;              // является ли пользователь редактором
    private static String status;               // статус пользователя
    public static boolean canInvite;            // может ли этот пользователь отправлять приглашения контактам
    public static Profile.Gifts gifts = new Profile.Gifts(); // массив подарков пользователя
    public static SparseArrayCompat<Profile.TopfaceNotifications> notifications;
    public static boolean giveNoviceLikes = false;

    // State
    public static long profileUpdateTime;               // время последнего вызова setProfile(...)
    public static boolean wasCityAsked = false;         // был ли показан экран выбора города новичку
    public static boolean needShowBonusCounter = false;
    private static AtomicBoolean mIsLoaded = new AtomicBoolean(false);

    private static void setProfileCache(final JSONObject response) {
        if (response != null) {
            SessionConfig config = App.getSessionConfig();
            config.setProfileData(response.toString());
            config.saveConfig();
        }
    }

    public static Profile getProfile() {
        Profile profile = new Profile();
        profile.uid = uid;
        profile.firstName = first_name;
        profile.age = age;
        profile.sex = sex;

        profile.notifications = notifications;
        profile.email = email;
        profile.emailConfirmed = emailConfirmed;
        profile.emailGrabbed = emailGrabbed;

        profile.premium = premium;
        profile.invisible = invisible;

        profile.city = city;

        profile.dating = dating;
        profile.forms = forms;
        profile.setStatus(status);
        profile.gifts = gifts;
        profile.background = background_id;

        profile.photos = photos;
        profile.photo = photo;
        profile.photosCount = totalPhotos;

        profile.paid = paid;
        profile.showAd = show_ad;
        profile.xstatus = xstatus;
        profile.setEditor(editor);
        profile.giveNoviceLikes = giveNoviceLikes;
        profile.canInvite = canInvite;
        return profile;
    }

    public static void setProfile(Profile profile, JSONObject response) {
        setProfile(profile, response, ProfileRequest.P_ALL);
    }

    public static void setProfile(Profile profile, JSONObject response, int part) {
        switch (part) {
            case ProfileRequest.P_NECESSARY_DATA:
                gifts = profile.gifts;
                invisible = profile.invisible;
                premium = profile.premium;
                show_ad = profile.showAd;
                photo = profile.photo;
                photos = profile.photos;
                totalPhotos = profile.photosCount;
                break;
            case ProfileRequest.P_ALL:
                Editor.init(profile);
                uid = profile.uid;
                first_name = profile.firstName;
                age = profile.age;
                sex = profile.sex;
                city = profile.city;

                notifications = profile.notifications;
                email = profile.email;
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
                giveNoviceLikes = profile.giveNoviceLikes;
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
            SessionConfig config = App.getSessionConfig();
            String profileCache = config.getProfileData();
            Profile profile;
            if (!TextUtils.isEmpty(profileCache)) {
                //Получаем опции из кэша
                try {
                    JSONObject profileJson = new JSONObject(profileCache);
                    profile = new Profile(profileJson);
                    setProfile(profile, profileJson);
                    result = true;
                } catch (JSONException e) {
                    config.resetProfileData();
                    Debug.error(e);
                }
            }
        }
        mIsLoaded.set(true);
        return result;
    }

    /**
     * Опции по умолчанию
     */
    private static Products mMarketProducts;
    private static ProductsDetails mProductsDetails;
    private static PaymentWallProducts mPWProducts;
    private static PaymentWallProducts mPWMobileProducts;

    /**
     * Данные из сервиса googleplay.getProducts
     * Внимание! Может возвращать null, если данный тип сборки не поддерживает покупки
     */
    public static Products getMarketProducts() {
        if (mMarketProducts == null) {
            SessionConfig config = App.getSessionConfig();
            String productsCache = config.getProductsData();
            if (!TextUtils.isEmpty(productsCache)) {
                //Получаем опции из кэша
                try {
                    mMarketProducts = new Products(
                            new JSONObject(productsCache)
                    );
                } catch (JSONException e) {
                    config.resetGoogleProductsData();
                    Debug.error(e);
                }
            }
        }
        return mMarketProducts;
    }

    public static ProductsDetails getMarketProductsDetails() {
        if (mProductsDetails == null) {
            SessionConfig config = App.getSessionConfig();
            String productsDetailsCache = config.getProductsDetailsData();
            if (!TextUtils.isEmpty(productsDetailsCache)) {
                //Получаем опции из кэша
                try {
                    mProductsDetails = JsonUtils.fromJson(productsDetailsCache, ProductsDetails.class);
                } catch (JsonSyntaxException e) {
                    config.resetGoogleProductsData();
                    Debug.error(e);
                }
            }
        }
        return mProductsDetails;
    }

    public static Products getPaymentWallProducts(PaymentWallProducts.TYPE type) {
        PaymentWallProducts products = type == PaymentWallProducts.TYPE.MOBILE ? mPWMobileProducts : mPWProducts;
        if (products == null) {
            SessionConfig config = App.getSessionConfig();
            String productsCache = config.getPaymentwallProductsData(type);
            if (!TextUtils.isEmpty(productsCache)) {
                //Получаем опции из кэша
                try {
                    products = new PaymentWallProducts(
                            new JSONObject(productsCache), type
                    );
                } catch (JSONException e) {
                    config.resetGoogleProductsData();
                    Debug.error(e);
                }
            }
        }
        return products;
    }

    public static boolean isDataFilled() {
        return city != null && !city.isEmpty() && age != 0 && first_name != null && photo != null;
    }

    /**
     * Clears CacheProfile fields (does not affect cached data from ProfileConfig)
     */
    public static void clearProfileAndOptions() {
        setProfile(new Profile(), null);
        wasCityAsked = false;
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

    public static void setOptions(final JSONObject response) {
        //Каждый раз не забываем кешировать запрос опций, но делаем это в отдельном потоке
        if (response != null) {
            SessionConfig config = App.getSessionConfig();
            config.setOptionsData(response.toString());
            config.saveConfig();
        }
    }

    public static void setMarketProducts(Products products, final JSONObject response) {
        mMarketProducts = products;
        //Каждый раз не забываем кешировать запрос продуктов, но делаем это в отдельном потоке
        if (response != null) {
            App.getSessionConfig().setMarketProductsData(response.toString());
        }
    }

    public static void setMarketProductsDetails(ProductsDetails productsDetails) {
        mProductsDetails = productsDetails;
        if (mProductsDetails != null) {
            App.getSessionConfig().setMarketProductsDetailsData(JsonUtils.toJson(mProductsDetails));
        }
    }

    public static void setPaymentwallProducts(PaymentWallProducts products, final JSONObject response, PaymentWallProducts.TYPE type) {
        switch (type) {
            case DIRECT:
                mPWProducts = products;
                break;
            case MOBILE:
                mPWMobileProducts = products;
                break;
        }


        //Каждый раз не забываем кешировать запрос продуктов, но делаем это в отдельном потоке
        if (response != null) {
            App.getSessionConfig().setPaymentWallProductsData(response.toString(), type);
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

    public static boolean needToChangePassword(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return preferences != null && preferences.getBoolean(Static.PREFERENCES_NEED_CHANGE_PASSWORD, false);
    }

    public static void onPasswordChanged(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_NEED_CHANGE_PASSWORD, false);
        editor.apply();
    }

    public static boolean needCityConfirmation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return preferences != null && preferences.getBoolean(Static.PREFERENCES_NEED_CITY_CONFIRM, false);
    }

    public static void onCityConfirmed(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_NEED_CITY_CONFIRM, false);
        editor.apply();
    }

    public static void onRegistration(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Static.PREFERENCES_NEED_CHANGE_PASSWORD, false);
        editor.putBoolean(Static.PREFERENCES_NEED_CITY_CONFIRM, true);
        editor.apply();
    }

    public static boolean isEditor() {
        return editor;
    }

    /**
     * Посылаем Broadcast о том, что данные профиля обновлены
     */
    public static void sendUpdateProfileBroadcast() {
        LocalBroadcastManager.getInstance(App.getContext())
                .sendBroadcast(new Intent(PROFILE_UPDATE_ACTION));
    }

    public static void selectCity(Activity activity) {
        CacheProfile.wasCityAsked = true;
        CacheProfile.onCityConfirmed(activity);
        activity.startActivityForResult(new Intent(activity, CitySearchActivity.class),
                CitySearchActivity.INTENT_CITY_SEARCH_AFTER_REGISTRATION);
    }

    public static boolean needToSelectCity(Context context) {
        return (
                !CacheProfile.isEmpty() &&
                        (
                                CacheProfile.city == null ||
                                        CacheProfile.city.isEmpty() ||
                                        CacheProfile.needCityConfirmation(context)
                        )
                        && !CacheProfile.wasCityAsked
        );
    }

    public static void incrementPhotoPosition(int diff, boolean needBroadcast) {
        if (CacheProfile.photo != null) {
            CacheProfile.photo.position += diff;
            if (needBroadcast) {
                Intent intent = new Intent(OwnAvatarFragment.UPDATE_AVATAR_POSITION);
                LocalBroadcastManager.getInstance(App.getContext())
                        .sendBroadcast(intent);
            }
        }
    }

    public static void incrementPhotoPosition(int diff) {
        incrementPhotoPosition(diff, true);
    }

    public static boolean isSetSympathiesBonus() {
        return giveNoviceLikes;
    }

    public static void completeSetNoviceSympathiesBonus() {
        giveNoviceLikes = false;
    }

}
