package com.topface.topface.ui.external_libs.appodeal

import android.os.Bundle
import android.support.annotation.IntDef

/**
 * Created by ppavlik on 03.07.17.
 * События от Appodeal
 */
data class AppodealEvent(@AppodealType val type: Long = UNDEFINED_TYPE,
                         @AppodealEventType val event: Long = UNDEFINED_EVENT,
                         val extra: Bundle = Bundle()) {
    companion object {
        const val UNDEFINED_TYPE = 0L
        const val COMMON_TYPE = 1L
        const val BANNER_VIEW_TYPE = 2L
        const val INTERSTITIAL_TYPE = 3L

        const val SDK_INIT_SUCCESFULL = -2L
        const val SDK_START_INIT = -1L

        const val UNDEFINED_EVENT = 0L

        const val BANNER_LOADED = 1L
        const val BANNER_FAILED_TO_LOAD = 2L
        const val BANNER_SHOWN = 3L
        const val BANNER_CLICKED = 4L

        const val INTERSTITIAL_LOADED = 5L
        const val INTERSTITIAL_FAILED_TO_LOAD = 6L
        const val INTERSTITIAL_SHOWN = 7L
        const val INTERSTITIAL_CLICKED = 8L
        const val INTERSTITIAL_CLOSED = 9L

        const val IS_PRECACHE = "AppodealEvent.Extra.IsPrecashe"
        const val HEIGHT = "AppodealEvent.Extra.Height"


        @IntDef(UNDEFINED_TYPE, COMMON_TYPE, BANNER_VIEW_TYPE, INTERSTITIAL_TYPE)
        annotation class AppodealType

        @IntDef(SDK_START_INIT, SDK_INIT_SUCCESFULL, UNDEFINED_EVENT, BANNER_LOADED,
                BANNER_FAILED_TO_LOAD, BANNER_SHOWN, BANNER_CLICKED, INTERSTITIAL_LOADED,
                INTERSTITIAL_FAILED_TO_LOAD, INTERSTITIAL_SHOWN, INTERSTITIAL_CLICKED, INTERSTITIAL_CLOSED)
        annotation class AppodealEventType

        fun onSdkStartInit() = AppodealEvent(COMMON_TYPE, SDK_START_INIT)
        fun onSdkInitSuccesfull() = AppodealEvent(COMMON_TYPE, SDK_INIT_SUCCESFULL)
        fun onBannerLoaded(height: Int, isPrecashe: Boolean) = AppodealEvent(BANNER_VIEW_TYPE, BANNER_LOADED, Bundle().apply {
            putInt(HEIGHT, height)
            putBoolean(IS_PRECACHE, isPrecashe)
        })

        fun onBannerFailedToLoad() = AppodealEvent(BANNER_VIEW_TYPE, BANNER_FAILED_TO_LOAD)
        fun onBannerShown() = AppodealEvent(BANNER_VIEW_TYPE, BANNER_SHOWN)
        fun onBannerClicked() = AppodealEvent(BANNER_VIEW_TYPE, BANNER_CLICKED)
        fun onInterstitialLoaded(isPrecashe: Boolean) = AppodealEvent(INTERSTITIAL_TYPE, INTERSTITIAL_LOADED, Bundle().apply {
            putBoolean(IS_PRECACHE, isPrecashe)
        })
        fun onInterstitialFailedToLoad() = AppodealEvent(INTERSTITIAL_TYPE, INTERSTITIAL_FAILED_TO_LOAD)
        fun onInterstitialShown() = AppodealEvent(INTERSTITIAL_TYPE, INTERSTITIAL_SHOWN)
        fun onInterstitialClicked() = AppodealEvent(INTERSTITIAL_TYPE, INTERSTITIAL_CLICKED)
        fun onInterstitialClosed() = AppodealEvent(INTERSTITIAL_TYPE, INTERSTITIAL_CLOSED)
    }
}