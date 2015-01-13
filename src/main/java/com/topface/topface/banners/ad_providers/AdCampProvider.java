package com.topface.topface.banners.ad_providers;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;

import ru.adcamp.ads.BannerAdView;

/**
 * Created by kirussell on 12/01/15.
 * Adcamp.ru
 */
class AdCampProvider extends AbstractAdsProvider {

    @Override
    public final boolean injectBannerInner(IPageWithAds page, final IAdProviderCallbacks callbacks) {
        if (isAvailable()) {
            return false;
        }
        ViewGroup container = page.getContainerForAd();
        final BannerAdView adView = (BannerAdView) View
                .inflate(container.getContext(), R.layout.banner_adcamp, container)
                .findViewById(R.id.adcampView);
        adView.setBannerAdViewListener(new BannerAdView.BannerAdViewListener() {
            @Override
            public void onLoadingStarted(BannerAdView bannerAdView) {
            }

            @Override
            public void onLoadingFailed(BannerAdView bannerAdView, String s) {
                callbacks.onFailedToLoadAd();
            }

            @Override
            public void onBannerDisplayed(BannerAdView bannerAdView) {
                adView.setVisibility(View.VISIBLE);
                callbacks.onAdLoadSuccess(bannerAdView);
            }

            @Override
            public void onBannerClicked(BannerAdView bannerAdView, String s) {
            }
        });
        adView.showAd();
        return true;
    }

    public boolean isAvailable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO
                || !CredentialsUtils.isAdcampInitialized();
    }
}
