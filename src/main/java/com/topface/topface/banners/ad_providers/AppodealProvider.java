package com.topface.topface.banners.ad_providers;

import android.app.Activity;
import android.view.ViewGroup;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.UserSettings;
import com.topface.topface.Static;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ads.FullscreenController;

public class AppodealProvider extends AbstractAdsProvider {

    @Override
    boolean injectBannerInner(final IPageWithAds page, final IAdProviderCallbacks callbacks) {
        Activity mActivity = page.getActivity();
        final ViewGroup container = page.getContainerForAd();
        Appodeal.initialize(mActivity, FullscreenController.APP_KEY, Appodeal.BANNER_VIEW);
        Appodeal.getUserSettings(mActivity)
                .setGender(
                        CacheProfile.getProfile().sex == Static.BOY ?
                                UserSettings.Gender.MALE :
                                UserSettings.Gender.FEMALE)
                .setAge(CacheProfile.getProfile().age);
        final BannerView adView =  Appodeal.getBannerView(page.getActivity());
        container.addView(adView);
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
