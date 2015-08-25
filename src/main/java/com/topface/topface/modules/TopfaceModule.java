package com.topface.topface.modules;

import android.location.Location;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.state.CacheDataInterface;
import com.topface.topface.state.CountersDataProvider;
import com.topface.topface.state.OptionsProvider;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.AddToLeaderActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.config.SessionConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.geo.GeoLocationManager;
import com.topface.topface.utils.social.AuthorizationManager;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppetr on 16/06/15.
 * module injecting AppState
 */
@Module(library = true,
        overrides = false,
        injects = {

                //Activites

                PurchasesActivity.class,
                AddToLeaderActivity.class,
                NavigationActivity.class,
                PaymentwallActivity.class,

                //Fragments

                AdmirationFragment.class,
                PeopleNearbyFragment.class,
                PurchasesFragment.class,
                DatingFragment.class,
                LikesFragment.class,
                MenuFragment.class,
                AdmirationFragment.class,
                PeopleNearbyFragment.class,
                PromoKey71Dialog.class,
                PromoKey81Dialog.class,

                //Other
                CountersDataProvider.class,
                AuthorizationManager.class,
                OptionsProvider.class,
                GeoLocationManager.class,
                App.class,
                CountersManager.class,
                OverflowMenu.class,
                Profile.class,
                Options.class
        }
)
public class TopfaceModule {

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    TopfaceAppState providesTopfaceAppState() {
        return new TopfaceAppState(new CacheDataInterface() {
            @Override
            public <T> void saveDataToCache(T data) {
                if (data.getClass() == BalanceData.class) {
                    BalanceData balanceData = (BalanceData) data;
                    CacheProfile.premium = balanceData.premium;
                    CacheProfile.likes = balanceData.likes;
                    CacheProfile.money = balanceData.money;
                } else if (data.getClass() == CountersData.class) {
                    CacheProfile.countersData = (CountersData) data;
                } else if (data.getClass() == Location.class) {
                    UserConfig config = App.getUserConfig();
                    config.setUserGeoLocation((Location) data);
                    config.saveConfig();
                }
                //кэш для Options сохраняем в SessionConfig в конце парсинга опций
            }


            public Options getOptions() {
                SessionConfig config = App.getSessionConfig();
                String optionsCache = config.getOptionsData();
                Options options = null;
                if (!TextUtils.isEmpty(optionsCache)) {
                    //Получаем опции из кэша, причем передаем флаг, что бы эти опции не кешировались повторно
                    try {
                        return new Options(new JSONObject(optionsCache), false);
                    } catch (JSONException e) {
                        //Если произошла ошибка при парсинге кэша, то скидываем опции
                        config.resetOptionsData();
                        Debug.error(e);
                    }
                }
                //Если по каким то причинам кэша нет и опции нам в данный момент взять негде.
                //то просто используем их по умолчанию
                return new Options(null, false);
            }

            @Override
            public <T> T getDataFromCache(Class<T> classType) {
                if (BalanceData.class.equals(classType)) {
                    return (T) new BalanceData(CacheProfile.premium, CacheProfile.likes, CacheProfile.money);
                } else if (CountersData.class.equals(classType)) {
                    return (T) (CacheProfile.countersData != null ? new CountersData(CacheProfile.countersData) : new CountersData());
                } else if (Options.class.equals(classType)) {
                    return (T) getOptions();
                }
                return null;
            }
        });
    }

}