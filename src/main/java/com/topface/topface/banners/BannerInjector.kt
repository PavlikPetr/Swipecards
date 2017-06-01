package com.topface.topface.banners

import com.topface.topface.App
import com.topface.topface.statistics.AdStatistics

/**
 * Created by ppavlik on 31.05.17.
 * Inject banner to container
 */
class BannerInjector : IBannerInjector {

    private val mProvidersFactory by lazy {
        AdProvidersFactory()
    }

    private var mCurrentAdsProvider: IAdsProvider? = null
    private var mBannerName = AdProvidersFactory.Companion.BANNER_NONE

    override fun injectBanner(bannerName: String, provider: IAdsProvider, container: IBannerAds) {
        mBannerName = bannerName
        mCurrentAdsProvider = provider
        showAd(container, provider)
    }

    override fun cleanUp() {
    }

    private fun showAd(page: IBannerAds, provider: IAdsProvider) {
        showAd(page, provider, false)
    }

    private fun showAd(page: IBannerAds, provider: IAdsProvider?, isFallbackAd: Boolean) {
        if (provider != null) {
            val injectInitiated = provider.injectBanner(page,
                    object : IAdsProvider.IAdProviderCallbacks {
                        override fun onAdLoadSuccess(adView: android.view.View) {
                        }

                        override fun onFailedToLoadAd(codeError: Int?) {
                            AdStatistics.sendBannerFailedToLoad(mBannerName, codeError)
                            cleanUp(page)
                            if (!isFallbackAd) {
                                injectGag(page)
                            }
                        }

                        override fun onAdClick() {
                            AdStatistics.sendBannerClicked(mBannerName)
                        }

                        override fun onAdShow() {
                            AdStatistics.sendBannerShown(mBannerName)
                        }
                    })
            if (!injectInitiated && !isFallbackAd) {
                injectGag(page)
            }
        }
    }

    private fun injectGag(page: IBannerAds) {
        mCurrentAdsProvider = mProvidersFactory.createProvider(App.get().options.fallbackTypeBanner)
        showAd(page, mCurrentAdsProvider, true)
    }

    private fun cleanUp(page: IBannerAds) {
        val container = page.getContainerForAd()
        if (container != null) {
            if (mCurrentAdsProvider != null) {
                mCurrentAdsProvider!!.clean(page)
            }
            unbindDrawables(container)
            container.removeAllViews()
        }
    }

    private fun unbindDrawables(view: android.view.View?) {
        view?.background?.callback = null
        if (view is android.view.ViewGroup) {
            for (i in 0..view.childCount - 1) {
                unbindDrawables(view.getChildAt(i))
            }
            view.removeAllViews()
        }
    }
}