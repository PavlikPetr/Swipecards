package com.topface.topface.banners.ad_providers;

import android.view.View;
import android.view.ViewGroup;

import com.appintop.adbanner.BannerAdContainer;
import com.appintop.adbanner.BannerListener;
import com.appintop.init.AdToApp;
import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.utils.ads.AdToAppController;

public class AdToAppProvider extends AbstractAdsProvider {

    public static final int REFRESH_INTERVAL = 60;

    @Override
    boolean injectBannerInner(final IPageWithAds page, final IAdProviderCallbacks callbacks) {
        AdToApp.initializeSDK(page.getActivity(),
                AdToAppController.ADTOAPP_APP_KEY,
                AdToApp.MASK_BANNER);
        ViewGroup container = page.getContainerForAd();
        final BannerAdContainer adView = (BannerAdContainer) View
                .inflate(container.getContext(), R.layout.banner_adtoapp, container)
                .findViewById(R.id.adtoapp_banner);
        adView.setRefreshInterval(REFRESH_INTERVAL);
        adView.setBannerListener(new BannerListener() {
            @Override
            public void onBannerLoad() {
                if (callbacks != null) {
                    callbacks.onAdLoadSuccess(adView);
                    callbacks.onAdShow();
                }
            }

            @Override
            public void onBannerFailedToLoad() {
                if (callbacks != null) {
                    callbacks.onFailedToLoadAd();
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
        return AdProvidersFactory.BANNER_ADTOAPP;
    }
}
