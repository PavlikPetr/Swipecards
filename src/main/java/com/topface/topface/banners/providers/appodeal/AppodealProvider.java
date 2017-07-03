package com.topface.topface.banners.providers.appodeal;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.UserSettings;
import com.appodeal.ads.utils.Log;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.AbstractAdsProvider;
import com.topface.topface.banners.AdProvidersFactory;
import com.topface.topface.banners.IBannerAds;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.FormItem;

public class AppodealProvider extends AbstractAdsProvider {

    public static final String APPODEAL_APP_KEY = "2f48418b677cf24a3fa37eacfc7a4e76d385db08b51bd328";

    @Override
    public boolean injectBannerInner(final IBannerAds page, final IAdProviderCallbacks callbacks) {
        Activity activity = page.getActivity();
        Appodeal.setTesting(false);
        Appodeal.setLogLevel(Log.LogLevel.verbose);
        Appodeal.initialize(activity, APPODEAL_APP_KEY, Appodeal.BANNER_VIEW);
        final BannerView adView = Appodeal.getBannerView(page.getActivity());
        page.getContainerForAd().addView(adView);
        UserSettings userSettings = Appodeal.getUserSettings(activity.getApplicationContext());
        userSettings.setGender(
                App.get().getProfile().sex == Profile.BOY ?
                        UserSettings.Gender.MALE :
                        UserSettings.Gender.FEMALE)
                .setAge(App.get().getProfile().age);
        if (Appodeal.isLoaded(Appodeal.BANNER_VIEW)) {
            bannerLoaded(page, callbacks, adView);
        }
        Appodeal.setBannerCallbacks(new BannerCallbacks() {

            @Override
            public void onBannerLoaded(int i, boolean b) {
                bannerLoaded(page, callbacks, adView);
            }

            @Override
            public void onBannerFailedToLoad() {
                if (callbacks != null) {
                    callbacks.onFailedToLoadAd(null);
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

    private void bannerLoaded(IBannerAds page, IAdProviderCallbacks callbacks, BannerView adView) {
        Appodeal.show(page.getActivity(), Appodeal.BANNER_VIEW);
        if (callbacks != null) {
            callbacks.onAdLoadSuccess(adView);
        }
    }

    public static void setCustomSegment() {
        String fullscreenSegment = App.getAppComponent().weakStorage().getAppodealFullscreenSegmentName();
        if (TextUtils.isEmpty(fullscreenSegment)) {
            String bannerSegment = App.getAppComponent().weakStorage().getAppodealBannerSegmentName();
            Debug.log("BANNER_SETTINGS : set segment " + bannerSegment);
            Appodeal.setCustomRule(bannerSegment, 0);
        } else {
            Debug.log("BANNER_SETTINGS : set segment " + fullscreenSegment);
            Appodeal.setCustomRule(fullscreenSegment, 0);
        }
    }
}
