package com.topface.topface.banners.ad_providers;

import android.view.View;
import android.view.ViewGroup;

import com.moneytapp.sdk.android.Ads;
import com.moneytapp.sdk.android.IBannerViewListener;
import com.moneytapp.sdk.android.datasource.responce.BaseResponse;
import com.moneytapp.sdk.android.view.BannerSize;
import com.moneytapp.sdk.android.view.BannerView;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;

class MoneyTappProvider extends AbstractAdsProvider {

    @Override
    public boolean injectBannerInner(IPageWithAds page, final IAdProviderCallbacks callbacks) {
        Ads.setDebugMode(true);
        ViewGroup container = page.getContainerForAd();
        final BannerView moneyTappBanner = (BannerView) View
                .inflate(container.getContext(), R.layout.banner_money_tapp, container)
                .findViewById(R.id.moneyTapView);
        moneyTappBanner.setPlaceId(App.getContext().getString(R.string.money_tapp_place_id));
        moneyTappBanner.setBannerSize(BannerSize.BANNER_SIZE_320x50);
        moneyTappBanner.setBannerViewListener(new IBannerViewListener() {
            @Override
            public void onBannerLoaded() {
                moneyTappBanner.setVisibility(View.VISIBLE);
                callbacks.onAdLoadSuccess(moneyTappBanner);
            }

            @Override
            public void onBannerLoadError(BaseResponse baseResponse) {
                callbacks.onFailedToLoadAd();
            }

            @Override
            public void onBannerClick() {

            }
        });
        moneyTappBanner.loadNewBanner();
        return true;
    }
}
