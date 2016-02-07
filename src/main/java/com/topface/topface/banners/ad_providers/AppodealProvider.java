package com.topface.topface.banners.ad_providers;

import android.app.Activity;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.UserSettings;
import com.topface.topface.BuildConfig;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;

public class AppodealProvider extends AbstractAdsProvider {

    public static final String APPODEAL_APP_KEY = "2f48418b677cf24a3fa37eacfc7a4e76d385db08b51bd328";

    @Override
    boolean injectBannerInner(final IPageWithAds page, final IAdProviderCallbacks callbacks) {
        Activity activity = page.getActivity();
        Appodeal.initialize(activity, APPODEAL_APP_KEY, Appodeal.BANNER_VIEW);
        final BannerView adView = Appodeal.getBannerView(page.getActivity());
        page.getContainerForAd().addView(adView);
        if (BuildConfig.DEBUG) {
            Appodeal.setTesting(true);
        }
        Appodeal.getUserSettings(activity)
                .setGender(
                        CacheProfile.getProfile().sex == Profile.BOY ?
                                UserSettings.Gender.MALE :
                                UserSettings.Gender.FEMALE)
                .setAge(CacheProfile.getProfile().age);
        // добавляем в UserSettings id социальной сети, в зависимости от типа текущей авторизации
        switch (AuthToken.getInstance().getSocialNet()) {
            case AuthToken.SN_VKONTAKTE:
                Appodeal.getUserSettings(activity).setVkId(AuthToken.getInstance().getUserSocialId());
                break;
            case AuthToken.SN_FACEBOOK:
                Appodeal.getUserSettings(activity).setFacebookId(AuthToken.getInstance().getUserSocialId());
                break;
        }
        Appodeal.setBannerCallbacks(new BannerCallbacks() {

            @Override
            public void onBannerLoaded() {
                Appodeal.show(page.getActivity(), Appodeal.BANNER_VIEW);
                if (callbacks != null) {
                    callbacks.onAdLoadSuccess(adView);
                }
            }

            @Override
            public void onBannerFailedToLoad() {
                if (callbacks != null) {
                    callbacks.onFailedToLoadAd();
                }
            }

            @Override
            public void onBannerShown() {
                if (callbacks != null) {
                    callbacks.onAdShow();
                }
            }

            @Override
            public void onBannerClicked() {
                if (callbacks != null) {
                    callbacks.onAdClick();
                }
            }
        });
        return true;
    }

    @Override
    public String getBannerName() {
        return AdProvidersFactory.BANNER_APPODEAL;
    }
}
