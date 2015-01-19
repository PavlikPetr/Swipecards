package com.topface.topface.banners;

/**
 * Created by kirussell on 11/01/15.
 * Basic interface for banner injectors
 *
 */
public interface IBannerInjector {

    /**
     * Inject banner to provided container
     * @param container page for ads
     */
    void injectBanner(IPageWithAds container);

    /**
     * Provide methods that can clean all injected stuff
     */
    void cleanUp();
}
