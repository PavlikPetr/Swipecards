package com.topface.topface.banners

/**
 * Created by ppavlik on 31.05.17.
 * Basic interface for banner injectors
 */
interface IBannerInjector {
    /**
     * Inject banner to provided container
     *
     * @param bannerName banner sdk name
     * @param provider provider for ads
     * @param container page for ads
     */
    fun injectBanner(bannerName: String, provider: IAdsProvider, container: IBannerAds)

    /**
     * Provide methods that can clean all injected stuff
     */
    fun cleanUp()
}