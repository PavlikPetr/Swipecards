package com.topface.topface.modules;

import com.topface.topface.App;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.promo.dialogs.PromoDialog;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.state.CacheDataInterface;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.AddToLeaderActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.FeedFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment;
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment;
import com.topface.topface.ui.fragments.feed.TabbedLikesFragment;
import com.topface.topface.ui.fragments.feed.TabbedVisitorsFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.geo.GeoLocationManager;

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
                TabbedFeedFragment.class,
                TabbedLikesFragment.class,
                AdmirationFragment.class,
                TabbedDialogsFragment.class,
                TabbedVisitorsFragment.class,
                NavigationActivity.class,
                ChatFragment.class,
                FeedFragment.class,
                DialogsFragment.class,
                BookmarksFragment.class,
                VisitorsFragment.class,
                FansFragment.class,
                MutualFragment.class,
                AdmirationFragment.class,
                PeopleNearbyFragment.class,
                PhotoBlogFragment.class,
                PromoDialog.class,
                PromoKey71Dialog.class,
                PromoKey81Dialog.class,
                PaymentwallActivity.class
        }
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
                }
            }

            @Override
            public <T> T getDataFromCache(Class<T> classType) {
                if (BalanceData.class.equals(classType)) {
                    return (T) new BalanceData(CacheProfile.premium, CacheProfile.likes, CacheProfile.money);
                } else if (CountersData.class.equals(classType)) {
                    return (T) (CacheProfile.countersData != null ? new CountersData(CacheProfile.countersData) : new CountersData());
                }
                return null;
            }
        });
    }

}
