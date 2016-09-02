package com.topface.topface.modules;

import android.location.Location;
import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.AuthTokenStateData;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.state.AuthState;
import com.topface.topface.state.CacheDataInterface;
import com.topface.topface.state.CountersDataProvider;
import com.topface.topface.state.DrawerLayoutState;
import com.topface.topface.state.EventBus;
import com.topface.topface.state.LifeCycleState;
import com.topface.topface.state.OptionsAndProfileProvider;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.bonus.presenter.BonusPresenter;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.external_libs.AdjustManager;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;
import com.topface.topface.ui.external_libs.modules.ExternalLibsInjectModule;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.ui.fragments.BaseAuthFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.OkProfileFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.TopfaceAuthFragment;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator;
import com.topface.topface.ui.fragments.feed.feed_di.FeedModule;
import com.topface.topface.ui.fragments.feed.likes.LikesLockScreenViewModel;
import com.topface.topface.ui.fragments.feed.mutual.MutualLockScreenViewModel;
import com.topface.topface.ui.fragments.profile.ProfilePhotoFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity;
import com.topface.topface.ui.views.DrawerLayoutManager;
import com.topface.topface.utils.ActivityLifeCycleReporter;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.FragmentLifeCycleReporter;
import com.topface.topface.utils.LifeCycleReporter;
import com.topface.topface.utils.NavigationManager;
import com.topface.topface.utils.RunningStateManager;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.config.SessionConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.startactions.ExpressMessageAction;
import com.topface.topface.utils.geo.FindAndSendCurrentLocation;
import com.topface.topface.utils.geo.GeoLocationManager;
import com.topface.topface.utils.popups.start_actions.ChooseCityPopupAction;
import com.topface.topface.utils.social.AuthorizationManager;
import com.topface.topface.utils.social.FbAuthorizer;
import com.topface.topface.utils.social.OkAuthorizer;
import com.topface.topface.utils.social.OkUserData;
import com.topface.topface.viewModels.AddToPhotoBlogHeaderViewModel;
import com.topface.topface.viewModels.AddToPhotoBlogViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppetr on 16/06/15.
 * module injecting AppState
 */
@Module(includes = {ExternalLibsInjectModule.class, FeedModule.class},
        injects = {
                PeopleNearbyFragment.class,
                GeoLocationManager.class,
                DatingFragment.class,
                CountersManager.class,
                OverflowMenu.class,
                PurchasesActivity.class,
                PhotoSwitcherActivity.class,
                PurchasesFragment.class,
                LikesFragment.class,
                MenuFragment.class,
                AdmirationFragment.class,
                NavigationActivity.class,
                PeopleNearbyFragment.class,
                PhotoBlogFragment.class,
                PromoKey71Dialog.class,
                PromoKey81Dialog.class,
                PaymentwallActivity.class,
                CountersDataProvider.class,
                FindAndSendCurrentLocation.class,
                AdjustManager.class,
                OkAuthorizer.class,
                OkProfileFragment.class,
                ProfilePhotoFragment.class,
                UserProfileFragment.class,
                TakePhotoPopup.class,
                AuthorizationManager.class,
                Profile.class,
                Options.class,
                User.class,
                OptionsAndProfileProvider.class,
                MenuFragment.class,
                NavigationManager.class,
                FragmentLifeCycleReporter.class,
                ActivityLifeCycleReporter.class,
                LifeCycleReporter.class,
                RunningStateManager.class,
                DrawerLayoutManager.class,
                DialogsFragment.class,
                BookmarksFragment.class,
                VisitorsFragment.class,
                FansFragment.class,
                AuthFragment.class,
                ChatActivity.class,
                AddPhotoHelper.class,
                FbAuthorizer.class,
                BaseAuthFragment.class,
                TopfaceAuthFragment.class,
                UserProfileFragment.class,
                BonusPresenter.class,
                ExpressMessageAction.class,
                AddToPhotoBlogViewModel.class,
                AddToPhotoBlogHeaderViewModel.class,
                LikesLockScreenViewModel.class,
                FeedNavigator.class,
                MutualLockScreenViewModel.class,
                DatingLockPopup.class,
                ChooseCityPopupAction.class
        },
        staticInjections = {
                AddPhotoHelper.class,
                App.class
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
                } else if (data.getClass() == OkUserData.class) {
                    UserConfig config = App.getUserConfig();
                    config.setOkUserData((OkUserData) data);
                    config.saveConfig();
                } else if (data.getClass() == AdjustAttributeData.class) {
                    AppConfig config = App.getAppConfig();
                    config.setAdjustAttributeData((AdjustAttributeData) data);
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
                } else if (Location.class.equals(classType)) {
                    return (T) App.getUserConfig().getUserGeoLocation();
                } else if (Options.class.equals(classType)) {
                    return (T) getOptions();
                } else if (Profile.class.equals(classType)) {
                    return (T) getProfile();
                } else if (OkUserData.class.equals(classType)) {
                    return (T) App.getUserConfig().getOkUserData();
                } else if (AdjustAttributeData.class.equals(classType)) {
                    return (T) App.getAppConfig().getAdjustAttributeData();
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
    RunningStateManager providesRunningStateManager() {
        return new RunningStateManager();
    }

    @Provides
    @Singleton
    NavigationState providesNavigationState() {
        return new NavigationState();
    }

    @Provides
    @Singleton
    LifeCycleState providesLifeCycleState() {
        return new LifeCycleState();
    }

    @Provides
    @Singleton
    DrawerLayoutState providesDrawerLayoutState() {
        return new DrawerLayoutState();
    }

    @Provides
    @Singleton
    EventBus providesEventBus() {
        return new EventBus();
    }

    @Provides
    @Singleton
    AuthState providesAuthState() {
        return new AuthState(new CacheDataInterface() {
            @Override
            public <T> void saveDataToCache(T data) {
            }

            @Override
            public <T> T getDataFromCache(Class<T> classType) {
                if (AuthTokenStateData.class.equals(classType)) {
                    return (T) (new AuthTokenStateData());
                }
                return null;
            }
        });
    }
}