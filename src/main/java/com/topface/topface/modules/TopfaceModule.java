package com.topface.topface.modules;

import android.location.Location;
import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.state.CacheDataInterface;
import com.topface.topface.state.CountersDataProvider;
import com.topface.topface.state.OptionsAndProfileProvider;
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
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.fragments.profile.ProfilePhotoFragment;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.ads.AdToAppController;
import com.topface.topface.utils.ads.AdToAppHelper;
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
                PhotoSwitcherActivity.class,

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
                ProfilePhotoFragment.class,

                //Other
                TopfaceAppState.class,
                CountersDataProvider.class,
                AuthorizationManager.class,
                OptionsAndProfileProvider.class,
                GeoLocationManager.class,
                App.class,
                CountersManager.class,
                OverflowMenu.class,
                Profile.class,
                Options.class,
                Profile.class,
                User.class,
                AdToAppHelper.class

        },
        staticInjections = {
                AddPhotoHelper.class
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
                    CacheProfile.balanceData = (BalanceData) data;
                } else if (data.getClass() == CountersData.class) {
                    CacheProfile.countersData = (CountersData) data;
                } else if (data.getClass() == Location.class) {
                    UserConfig config = App.getUserConfig();
                    config.setUserGeoLocation((Location) data);
                    config.saveConfig();
                } else if (data.getClass() == Options.class) {
                    CacheProfile.setOptions(JsonUtils.optionsToJson((Options) data));
                } else if (data.getClass() == Profile.class) {
                    Profile profile = (Profile) data;
                    CacheProfile.setProfile(profile, JsonUtils.profileToJson(profile));
                }
            }

            @Override
            public <T> T getDataFromCache(Class<T> classType) {
                if (BalanceData.class.equals(classType)) {
                    return (T) (CacheProfile.balanceData != null ? new BalanceData(CacheProfile.balanceData) : new BalanceData());
                } else if (CountersData.class.equals(classType)) {
                    return (T) (CacheProfile.countersData != null ? new CountersData(CacheProfile.countersData) : new CountersData());
                } else if (Options.class.equals(classType)) {
                    return (T) getOptions();
                } else if (Profile.class.equals(classType)) {
                    return (T) getProfile();
                }
                return null;
            }

            private Options getOptions() {
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

            private Profile getProfile() {
                SessionConfig config = App.getSessionConfig();
                String profileCache = config.getProfileData();
                if (!TextUtils.isEmpty(profileCache)) {
                    //Получаем опции из кэша
                    try {
                        JSONObject profileJson = new JSONObject(profileCache);
                        CacheProfile.isLoaded.set(true);
                        return new Profile(profileJson, true);
                    } catch (JSONException e) {
                        config.resetProfileData();
                        Debug.error(e);
                    }
                }
                return new Profile();
            }

        });
    }

    @Provides
    @Singleton
    AdToAppController providesAdToAppController() {
        return new AdToAppController();
    }
}