package com.topface.topface.banners;

import com.topface.topface.banners.ad_providers.AdProvidersFactory;

/**
 * Controls banners injection for given page
 */
public class BannersController {

    private IBannerInjector mFeedBannersInjector;

    public BannersController(IPageWithAds page) {
        super();
        getFeedBannerController().injectBanner(page);
    }

    public void onDestroy() {
        if (mFeedBannersInjector != null) mFeedBannersInjector.cleanUp();
    }

    public IBannerInjector getFeedBannerController() {
        if (mFeedBannersInjector == null) {
            mFeedBannersInjector = new BannerInjector(new AdProvidersFactory());
        }
        return mFeedBannersInjector;
    }
}
