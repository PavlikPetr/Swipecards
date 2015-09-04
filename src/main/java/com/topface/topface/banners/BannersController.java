package com.topface.topface.banners;

import com.topface.topface.banners.ad_providers.AdProvidersFactory;

/**
 * Controls banners injection for given page
 */
public class BannersController {

    private IBannerInjector mFeedBannersInjector;

    public BannersController(IPageWithAds page, boolean canShow) {
        super();
        if (page.getPageName() == PageInfo.PageName.LIKES_TABS) {
            if (canShow) {
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
