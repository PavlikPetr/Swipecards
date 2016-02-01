package com.topface.topface.banners.ad_providers;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.appintop.adbanner.BannerAdContainer;
import com.appintop.adbanner.BannerListener;
import com.appintop.init.AdToApp;
import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;

public class AdToAppProvider extends AbstractAdsProvider {

    public static final String ADTOAPP_APP_KEY = "361e95a8-3cf4-494d-89de-1a0f57f25ab3:b8942ef1-6fe1-4c7b-ab3d-2814072cedf3";
    public static final int REFRESH_INTERVAL = 60;

    @Override
    boolean injectBannerInner(final IPageWithAds page, final IAdProviderCallbacks callbacks) {
        Activity activity = page.getActivity();
        AdToApp.initializeSDK(activity,
                ADTOAPP_APP_KEY,
                AdToApp.MASK_BANNER);
        ViewGroup container = page.getContainerForAd();
        final BannerAdContainer adView = (BannerAdContainer) View
                .inflate(container.getContext(), R.layout.banner_adtoapp, container)
                .findViewById(R.id.adtoapp_banner);
        adView.setRefreshInterval(REFRESH_INTERVAL);
        adView.setBannerListener(new BannerListener() {
            @Override
            public void onBannerLoad() {
                callbacks.onAdLoadSuccess(adView);
                callbacks.onAdShow();
            }

            @Override
            public void onBannerFailedToLoad() {
                callbacks.onFailedToLoadAd();
            }

            @Override
            public void onBannerClicked() {
                callbacks.onAdClick();
            }
        });
        return true;
    }

    @Override
    public String getBannerName() {
        return AdProvidersFactory.BANNER_ADTOAPP;
    }
}
