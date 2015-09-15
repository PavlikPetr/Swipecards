package com.topface.topface.banners;

import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.data.Options;

/**
 * Controls banners injection for given page
 */
public class BannersController {

    private IBannerInjector mFeedBannersInjector;

    public BannersController(IPageWithAds page, Options options) {
        super();
        if (page.getPageName() == PageInfo.PageName.LIKES_TABS) {
            if (options.interstitial.canShow()) {
                return;
            }
        }
        getFeedBannerController(page).injectBanner(page);
    }

    public void onDestroy() {
        if (mFeedBannersInjector != null) mFeedBannersInjector.cleanUp();
    }

    public IBannerInjector getFeedBannerController(IPageWithAds page) {
        if (mFeedBannersInjector == null) {
            mFeedBannersInjector = new BannerInjector(new AdProvidersFactory(), page.getActivity());
        }
        return mFeedBannersInjector;
    }
}
