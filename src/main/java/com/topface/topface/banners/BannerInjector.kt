package com.topface.topface.banners

import android.view.View
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

    private fun showAd(page: IBannerAds, provider: IAdsProvider) = showAd(page, provider, false)

    private fun showAd(page: IBannerAds, provider: IAdsProvider?, isFallbackAd: Boolean) {
        provider?.let {
            val injectInitiated = it.injectBanner(page,
                    object : IAdsProvider.IAdProviderCallbacks {
                        override fun onAdLoadSuccess(adView: View) {}

                        override fun onFailedToLoadAd(codeError: Int?) {
                            AdStatistics.sendBannerFailedToLoad(mBannerName, codeError)
                            cleanUp(page)
                            if (!isFallbackAd) {
                                injectGag(page)
                            }
                        }

                        override fun onAdClick() = AdStatistics.sendBannerClicked(mBannerName)

                        override fun onAdShow() = AdStatistics.sendBannerShown(mBannerName)
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

    private fun cleanUp(page: IBannerAds) =
            page.getContainerForAd()?.let {
                mCurrentAdsProvider?.clean(page)
                unbindDrawables(it)
                it.removeAllViews()
            }

    private fun unbindDrawables(view: View?) {
        view?.background?.callback = null
        if (view is android.view.ViewGroup) {
            for (i in 0..view.childCount - 1) {
                unbindDrawables(view.getChildAt(i))
            }
            view.removeAllViews()
        }
    }
}