package com.topface.topface.banners.ad_providers;

import android.app.Activity;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.UserSettings;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.Static;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.data.Profile;

public class AppodealProvider extends AbstractAdsProvider {

    public static final String APPODEAL_APP_KEY = "2f48418b677cf24a3fa37eacfc7a4e76d385db08b51bd328";

    @Override
    boolean injectBannerInner(final IPageWithAds page, final IAdProviderCallbacks callbacks) {
        Activity activity = page.getActivity();
        Profile profile = App.from(page.getActivity()).getProfile();
        Appodeal.initialize(activity, APPODEAL_APP_KEY, Appodeal.BANNER_VIEW);
        final BannerView adView = Appodeal.getBannerView(page.getActivity());
        page.getContainerForAd().addView(adView);
        if (BuildConfig.DEBUG) {
            Appodeal.setTesting(true);
        }
        Appodeal.getUserSettings(activity)
                .setGender(
                        profile.sex == Static.BOY ?
                                UserSettings.Gender.MALE :
                                UserSettings.Gender.FEMALE)
                .setAge(profile.age);
        Appodeal.setBannerCallbacks(new BannerCallbacks() {

            @Override
            public void onBannerLoaded() {
                Appodeal.show(page.getActivity(), Appodeal.BANNER_VIEW);
                callbacks.onAdLoadSuccess(adView);
            }

            @Override
            public void onBannerFailedToLoad() {
                callbacks.onFailedToLoadAd();
            }

            @Override
            public void onBannerShown() {
            }

            @Override
            public void onBannerClicked() {
            }
        });
        return true;
    }

}
