package com.topface.topface.modules;

import android.location.Location;

import com.topface.topface.App;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.state.CacheDataInterface;
import com.topface.topface.state.CountersDataProvider;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.AddToLeaderActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.OkProfileFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.RunningStateManager;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.ads.AdToAppController;
import com.topface.topface.utils.ads.AdToAppHelper;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.geo.FindAndSendCurrentLocation;
import com.topface.topface.utils.geo.GeoLocationManager;
import com.topface.topface.utils.social.CurrentUser;
import com.topface.topface.utils.social.OkUserData;

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
                PeopleNearbyFragment.class,
                GeoLocationManager.class,
                App.class,
                DatingFragment.class,
                CountersManager.class,
                OverflowMenu.class,
                PurchasesActivity.class,
                PurchasesFragment.class,
                AddToLeaderActivity.class,
                LikesFragment.class,
                MenuFragment.class,
                AdmirationFragment.class,
                NavigationActivity.class,
                ChatFragment.class,
                DialogsFragment.class,
                BookmarksFragment.class,
                VisitorsFragment.class,
                FansFragment.class,
                MutualFragment.class,
                AdmirationFragment.class,
                PeopleNearbyFragment.class,
                PhotoBlogFragment.class,
                PromoKey71Dialog.class,
                PromoKey81Dialog.class,
                PaymentwallActivity.class,
                CountersDataProvider.class,
                AdToAppHelper.class,
                FindAndSendCurrentLocation.class,
                CurrentUser.class,
                OkProfileFragment.class
        },
        staticInjections = App.class
)
public class TopfaceModule {

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
                } else if (data.getClass() == OkUserData.class) {
                    UserConfig config = App.getUserConfig();
                    config.setOkUserData((OkUserData) data);
                    config.saveConfig();
                }
            }

            @Override
            public <T> T getDataFromCache(Class<T> classType) {
                if (BalanceData.class.equals(classType)) {
                    return (T) new BalanceData(CacheProfile.premium, CacheProfile.likes, CacheProfile.money);
                } else if (CountersData.class.equals(classType)) {
                    return (T) (CacheProfile.countersData != null ? new CountersData(CacheProfile.countersData) : new CountersData());
                } else if (Location.class.equals(classType)) {
                    return (T) App.getUserConfig().getUserGeoLocation();
                } else if (OkUserData.class.equals(classType)) {
                    return (T) App.getUserConfig().getOkUserData();
                }
                return null;
            }
        });
    }

    @Provides
    @Singleton
    AdToAppController providesAdToAppController() {
        return new AdToAppController();
    }

    @Provides
    @Singleton
    RunningStateManager providesRunningStateManager() {
        return new RunningStateManager();
    }
}