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
import com.topface.topface.ui.external_libs.appodeal.AppodealManager;
import com.topface.topface.ui.external_libs.appodeal.IBanner;
import com.topface.topface.ui.external_libs.appodeal.IFullscreen;
import com.topface.topface.utils.FormItem;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class AppodealProvider extends AbstractAdsProvider {

    public static final String APPODEAL_APP_KEY = "2f48418b677cf24a3fa37eacfc7a4e76d385db08b51bd328";

    private AppodealManager mAppodealManager;
    private IBanner mBannerCallback;

    @Override
    public boolean injectBannerInner(final IBannerAds page, final IAdProviderCallbacks callbacks) {
        Activity activity = page.getActivity();
        mAppodealManager = App.getAppComponent().appodealManager();
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
        mBannerCallback = new IBanner() {

            @Override
            public void initSuccessfull() {
            }

            @Override
            public void startInit() {
            }

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
        };
        mAppodealManager.addBannerCallback(mBannerCallback);
        return true;
    }

    @Override
    public String getBannerName() {
        return AdProvidersFactory.BANNER_APPODEAL;
    }

    private void bannerLoaded(IBannerAds page, IAdProviderCallbacks callbacks, BannerView adView) {
        mAppodealManager.showBanner(page.getActivity());
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

    @Override
    public void clean(@NotNull IBannerAds page) {
        mAppodealManager.removeBannerCallback(mBannerCallback);
        mAppodealManager.hideBanner(page.getActivity());
    }
}
