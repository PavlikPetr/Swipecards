package com.topface.topface.banners

/**
 * Created by ppavlik on 31.05.17.
 * Wrappers over ad providers
 * Asynchronously loads ad and then returns banner view
 */
interface IAdsProvider {

    /**
     * Starts load ad and returns banner view after loading through callback

     * @param page      to inject banner
     * *
     * @param callbacks result on ads loading
     * *
     * @return true if loading initiated successfully
     */
    fun injectBanner(page: IBannerAds, callbacks: IAdsProvider.IAdProviderCallbacks): Boolean

    fun getBannerName(): String

    interface IAdProviderCallbacks {
        fun onAdLoadSuccess(adView: android.view.View)

        fun onFailedToLoadAd(codeError: Int?)

        fun onAdClick()

        fun onAdShow()
    }

    fun clean(page: IBannerAds)
}