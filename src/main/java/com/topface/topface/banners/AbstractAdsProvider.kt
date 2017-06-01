package com.topface.topface.banners

/**
 * Created by ppavlik on 31.05.17.
 * Abstract over Ads provider interface with common injection logic
 */
abstract class AbstractAdsProvider : IAdsProvider {
    abstract fun injectBannerInner(page: IBannerAds, callbacks: IAdsProvider.IAdProviderCallbacks): Boolean

    override fun injectBanner(page: IBannerAds, callbacks: IAdsProvider.IAdProviderCallbacks): Boolean {
        return isAvailable(page) && injectBannerInner(page, callbacks)
    }

    fun isAvailable(page: IBannerAds): Boolean {
        return page.getContainerForAd() != null
    }

    override fun clean(page: IBannerAds) {}
}