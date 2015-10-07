package com.topface.topface.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.data.ProductsDetails;
import com.topface.topface.data.Profile;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.ui.fragments.OwnAvatarFragment;
import com.topface.topface.utils.config.SessionConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/* Cache Profile */
public class CacheProfile {

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
    public static BalanceData balanceData;

    // State
    public static long profileUpdateTime;               // время последнего вызова setProfile(...)
    public static boolean wasCityAsked = false;         // был ли показан экран выбора города новичку
    public static boolean needShowBonusCounter = false;
    public static AtomicBoolean isLoaded = new AtomicBoolean(false);

    private static void setProfileCache(final String profile) {
        if (profile != null) {
            SessionConfig config = App.getSessionConfig();
            config.setProfileData(profile);
            config.saveConfig();
        }
    }

    public static void setProfile(Profile profile, String profileStr) {
        CacheProfile.isLoaded.set(true);
        Editor.init(profile);
        setProfileCache(profileStr);
        setProfileUpdateTime();
    }

    public static String getStatus(Context context) {
        return App.from(context).getProfile().status;
    }

    public static void setStatus(Context context, String status) {
        App.from(context).getProfile().status = Profile.normilizeStatus(status);
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

    public static boolean isDataFilled(Context context) {
        Profile profile = App.from(context).getProfile();
        return profile.city != null && !profile.city.isEmpty() && profile.age != 0 && profile.firstName != null && profile.photo != null;
    }

    /**
     * Clears CacheProfile fields (does not affect cached data from ProfileConfig)
     */
    public static void clearProfileAndOptions(TopfaceAppState state) {
        state.destroyObservable(Profile.class);
        setProfileCache(null);
        wasCityAsked = false;
    }

    private static void setProfileUpdateTime() {
        profileUpdateTime = System.currentTimeMillis();
    }

    public static boolean isLoaded() {
        return isLoaded.get();
    }

    public static boolean isEmpty(Context context) {
        return App.from(context).getProfile().uid == 0;
    }

    public static void setOptions(final String options) {
        //Каждый раз не забываем кешировать запрос опций, но делаем это в отдельном потоке
        if (options != null) {
            SessionConfig config = App.getSessionConfig();
            config.setOptionsData(options.toString());
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

    public static String getUserNameAgeString(Profile profile) {
        return profile.firstName +
                (CacheProfile.isAgeOk(profile.age) ? ", " + profile.age : "");
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
        Profile profile = App.from(context).getProfile();
        return (
                !CacheProfile.isEmpty(context) &&
                        (
                                profile.city == null ||
                                        profile.city.isEmpty() ||
                                        CacheProfile.needCityConfirmation(context)
                        )
                        && !CacheProfile.wasCityAsked
        );
    }

    public static void incrementPhotoPosition(Context context, int diff, boolean needBroadcast) {
        Profile profile = App.from(context).getProfile();
        if (profile.photo != null) {
            profile.photo.position += diff;
            if (needBroadcast) {
                Intent intent = new Intent(OwnAvatarFragment.UPDATE_AVATAR_POSITION);
                LocalBroadcastManager.getInstance(App.getContext())
                        .sendBroadcast(intent);
            }
        }
    }

    public static void incrementPhotoPosition(Context context, int diff) {
        incrementPhotoPosition(context, diff, true);
    }

    public static void completeSetNoviceSympathiesBonus(Context context) {
        Profile profile = App.from(context).getProfile();
        profile.giveNoviceLikes = false;
    }

}
