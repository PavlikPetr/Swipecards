package com.topface.topface.banners;

import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.utils.CacheProfile;

/**
 * Controls banners injection for given page
 */
public class BannersController {

    private IBannerInjector mFeedBannersInjector;

    public BannersController(IPageWithAds page) {
        super();
        if (page.getPageName() == PageInfo.PageName.LIKES_TABS) {
            if (CacheProfile.getOptions().interstitial.canShow()) {
                return;
            }
        }
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
