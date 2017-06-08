package com.topface.topface.banners.providers

import com.ampiri.sdk.Ampiri
import com.ampiri.sdk.banner.InterstitialAd
import com.ampiri.sdk.banner.StandardAd
import com.ampiri.sdk.listeners.InterstitialAdCallback
import com.ampiri.sdk.listeners.StandardAdCallback
import com.ampiri.sdk.mediation.BannerSize
import com.ampiri.sdk.mediation.Gender
import com.ampiri.sdk.mediation.ResponseStatus
import com.appodeal.ads.InterstitialCallbacks
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.banners.AdProvidersFactory
import com.topface.topface.banners.AbstractAdsProvider
import com.topface.topface.banners.IAdsProvider
import com.topface.topface.banners.IBannerAds
import com.topface.topface.data.Profile
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate

class AmpiriProvider : AbstractAdsProvider() {
    companion object {
        fun createFullScreen(activity: android.app.Activity, callbacks: InterstitialCallbacks) =
                InterstitialAd(activity, App.getAppComponent().weakStorage().ampiriFullscreenSegmentName,
                        object : InterstitialAdCallback {
                            override fun onAdFailed(p0: InterstitialAd, p1: ResponseStatus) =
                                    callbacks.onInterstitialFailedToLoad()

                            override fun onAdClicked(p0: InterstitialAd) = callbacks.onInterstitialClicked()
                            override fun onAdLoaded(p0: InterstitialAd) = callbacks.onInterstitialLoaded(false)
                            override fun onAdOpened(p0: InterstitialAd) = callbacks.onInterstitialShown()
                            override fun onAdClosed(p0: InterstitialAd) = callbacks.onInterstitialClosed()
                        }).apply { AmpiriProvider.Companion.fillUserSettings() }

        private fun fillUserSettings() {
            with(java.util.Calendar.getInstance()) {
                val birthYear = get(java.util.Calendar.YEAR) - App.get().profile.age
                if (birthYear > 0) {
                    set(java.util.Calendar.YEAR, birthYear)
                    Ampiri.setUserBirthday(time)
                }
            }
            Ampiri.setUserGender(if (App.get().profile.sex == Profile.BOY) Gender.MALE else Gender.FEMALE)
            // todo fill users interests if we can get some from profile
            //Ampiri.setUserInterests(null)
        }
    }

    override fun injectBannerInner(page: IBannerAds, callbacks: IAdsProvider.IAdProviderCallbacks): Boolean =
            with(page) {
                getContainerForAd()?.findViewById(R.id.ampiri_banner_stub)?.let {
                    AmpiriProvider.Companion.fillUserSettings()
                    val adView = (it as android.view.ViewStub).inflate() as android.widget.FrameLayout
                    mAmpiriBannerLifeCycler = AmpiriProvider.AmpiriBannerLifeCycler(
                            StandardAd(getActivity(), adView,
                                    App.getAppComponent().weakStorage().ampiriBannerSegmentName,
                                    BannerSize.BANNER_SIZE_320x50,
                                    object : StandardAdCallback {
                                        override fun onAdFailed(p0: StandardAd, p1: ResponseStatus) =
                                                callbacks.onFailedToLoadAd(null)

                                        override fun onAdClicked(p0: StandardAd) = callbacks.onAdClick()
                                        override fun onAdLoaded(p0: StandardAd) = callbacks.onAdLoadSuccess(adView)
                                        override fun onAdOpened(p0: StandardAd) = callbacks.onAdShow()
                                        override fun onAdClosed(p0: StandardAd) {}
                                    })
                    )
                    mAmpiriBannerLifeCycler?.let {
                        getActivity().registerLifeCycleDelegate(it)
                        it.ads.loadAd()
                    }
                    return@with true
                }
                return@with false
            }

    override fun clean(page: IBannerAds) {
        mAmpiriBannerLifeCycler?.let {
            it.onDestroy()
            page.getActivity().unregisterLifeCycleDelegate(it)
        }
    }

    override fun getBannerName() = AdProvidersFactory.BANNER_AMPIRI

    private var mAmpiriBannerLifeCycler: AmpiriProvider.AmpiriBannerLifeCycler? = null

    private class AmpiriBannerLifeCycler(val ads: StandardAd) : ILifeCycle {
        override fun onResume() = ads.onActivityResumed()
        override fun onPause() = ads.onActivityPaused()
        fun onDestroy() = ads.onActivityDestroyed()
    }

    class AmpiriInterstitialLifeCycler(val ads: InterstitialAd) : ILifeCycle {
        override fun onResume() = ads.onActivityResumed()
        override fun onPause() = ads.onActivityPaused()
        fun onDestroy() = ads.onActivityDestroyed()
    }
}