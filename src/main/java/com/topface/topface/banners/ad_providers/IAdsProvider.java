package com.topface.topface.banners.ad_providers;

import android.view.View;

import com.topface.topface.banners.IPageWithAds;

/**
 * Created by kirussell on 11/01/15.
 * Wrappers over ad providers
 * Asynchronously loads ad and then returns banner view
 */
public interface IAdsProvider {

    /**
     * Starts load ad and returns banner view after loading through callback
     * @param page to inject banner
     * @param callbacks result on ads loading
     * @return true if loading initiated successfully
     */
    boolean injectBanner(IPageWithAds page, IAdProviderCallbacks callbacks);

    public interface IAdProviderCallbacks {
        void onAdLoadSuccess(View adView);
        void onFailedToLoadAd();
    }
}
