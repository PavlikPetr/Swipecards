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
    private var mPage: IBannerAds? = null
    private var mBannerName = AdProvidersFactory.Companion.BANNER_NONE

    override fun injectBanner(bannerName: String, provider: IAdsProvider, container: IBannerAds) {
        mPage = container
        mBannerName = bannerName
        mCurrentAdsProvider = provider
        showAd(provider)
    }

    override fun cleanUp() =
            mPage?.let { page ->
                page.getContainerForAd()?.let {
                    mCurrentAdsProvider?.clean(page)
                    unbindDrawables(it)
                    it.removeAllViews()
                }
            } ?: Unit


    private fun showAd(provider: IAdsProvider) = showAd(provider, false)

    private fun showAd(provider: IAdsProvider?, isFallbackAd: Boolean) {
        provider?.let { provider ->
            mPage?.let {
                val injectInitiated = provider.injectBanner(it,
                        object : IAdsProvider.IAdProviderCallbacks {
                            override fun onAdLoadSuccess(adView: View) {}

                            override fun onFailedToLoadAd(codeError: Int?) {
                                AdStatistics.sendBannerFailedToLoad(mBannerName, codeError)
                                cleanUp()
                                if (!isFallbackAd) {
                                    injectGag()
                                }
                            }

                            override fun onAdClick() = AdStatistics.sendBannerClicked(mBannerName)

                            override fun onAdShow() = AdStatistics.sendBannerShown(mBannerName)
                        })
                if (!injectInitiated && !isFallbackAd) {
                    injectGag()
                }
            }
        }
    }

    private fun injectGag() {
        mCurrentAdsProvider = mProvidersFactory.createProvider(App.get().options.fallbackTypeBanner)
        showAd(mCurrentAdsProvider, true)
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