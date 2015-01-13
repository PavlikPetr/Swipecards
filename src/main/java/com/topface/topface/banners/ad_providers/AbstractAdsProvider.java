package com.topface.topface.banners.ad_providers;

import com.topface.topface.banners.IPageWithAds;

/**
 * Created by kirussell on 12/01/15.
 * Abstract over Ads provider interface with common injection logic
 */
abstract class AbstractAdsProvider implements IAdsProvider {

    abstract boolean injectBannerInner(IPageWithAds page, IAdProviderCallbacks callbacks);

    @Override
    public final boolean injectBanner(IPageWithAds page, IAdProviderCallbacks callbacks) {
        return isAvailable(page) && injectBannerInner(page, callbacks);
    }

    public final boolean isAvailable(IPageWithAds page) {
        return page.getContainerForAd() != null;
    }
}
