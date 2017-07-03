package com.topface.topface.ui.external_libs.appodeal

import android.app.Activity
import android.support.annotation.IntDef
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.utils.Log
import com.topface.topface.utils.rx.shortSubscription
import rx.Emitter
import rx.Observable
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by ppavlik on 03.07.17.
 * Менеджер для работы с sdk Appodeal
 */
class AppodealManager {

    companion object {
        val APPODEAL_APP_KEY = "2f48418b677cf24a3fa37eacfc7a4e76d385db08b51bd328"
        private const val TAG = "AppodealManager"

        private const val NOT_INITED = 0L
        private const val INIT_IN_PROGRESS = 1L
        private const val INITED_SUCCESS = 2L

        @IntDef(NOT_INITED, INIT_IN_PROGRESS, INITED_SUCCESS)
        annotation class AppodealInitState
    }

    val appodealObservable: Observable<AppodealEvent>

    private val mIsInitSuccess = AtomicLong(NOT_INITED)
    private var mShowBanner = {}
    private var mShowFullscreen = {}
    private var mEmitter: Emitter<AppodealEvent>? = null

    init {
        appodealObservable = Observable.fromEmitter<AppodealEvent>({
            mEmitter = it
        }, Emitter.BackpressureMode.LATEST).share()
        Appodeal.setBannerCallbacks(object : BannerCallbacks {
            override fun onBannerShown() {
                mEmitter?.onNext(AppodealEvent.onBannerShown())
            }

            override fun onBannerLoaded(height: Int, isPrecashe: Boolean) {
                mEmitter?.onNext(AppodealEvent.onBannerLoaded(height, isPrecashe))
            }

            override fun onBannerClicked() {
                mEmitter?.onNext(AppodealEvent.onBannerClicked())
            }

            override fun onBannerFailedToLoad() {
                mEmitter?.onNext(AppodealEvent.onBannerFailedToLoad())
            }
        })
        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialLoaded(isPrecashe: Boolean) {
                mEmitter?.onNext(AppodealEvent.onInterstitialLoaded(isPrecashe))
            }

            override fun onInterstitialShown() {
                mEmitter?.onNext(AppodealEvent.onInterstitialShown())
            }

            override fun onInterstitialClicked() {
                mEmitter?.onNext(AppodealEvent.onInterstitialClicked())
            }

            override fun onInterstitialFailedToLoad() {
                mEmitter?.onNext(AppodealEvent.onInterstitialFailedToLoad())
            }

            override fun onInterstitialClosed() {
                mEmitter?.onNext(AppodealEvent.onInterstitialClosed())
            }
        })
    }

    fun initAppodeal(activity: Activity) {
        if (getInitStatus() == NOT_INITED) {
            appodealObservable
                    .filter { it.event == INITED_SUCCESS }
                    .first()
                    .subscribe(shortSubscription {
                        mShowBanner.invoke()
                        mShowFullscreen.invoke()
                        mShowBanner = {}
                        mShowFullscreen = {}
                    })
            mEmitter?.onNext(AppodealEvent.onSdkStartInit())
            mIsInitSuccess.set(INIT_IN_PROGRESS)
            Appodeal.setTesting(false)
            Appodeal.setLogLevel(Log.LogLevel.verbose)
            Appodeal.setAutoCache(Appodeal.INTERSTITIAL or Appodeal.BANNER_VIEW, true)
            Appodeal.initialize(activity, APPODEAL_APP_KEY, Appodeal.INTERSTITIAL or Appodeal.BANNER_VIEW)
            initBanner()
            initFullscreen()
            mEmitter?.onNext(AppodealEvent.onSdkInitSuccesfull())
            mIsInitSuccess.set(INITED_SUCCESS)
        }
    }

    private fun initBanner() {
    }

    private fun initFullscreen() {

    }

    fun showBanner(activity: Activity) {
        if (isInitSuccess()) {
            Appodeal.show(activity, Appodeal.BANNER_VIEW)
        } else {
            mShowBanner = { showBanner(activity) }
            initAppodeal(activity)
        }
    }


    fun hideBanner(activity: Activity) {
        Appodeal.hide(activity, Appodeal.BANNER_VIEW)
    }

    fun showFullscreen(activity: Activity) {
        if (isInitSuccess()) {
            Appodeal.show(activity, Appodeal.INTERSTITIAL)
        } else {
            mShowFullscreen = { showBanner(activity) }
            initAppodeal(activity)
        }
    }

    fun isInitSuccess() = mIsInitSuccess.get() == INITED_SUCCESS

    @AppodealInitState
    fun getInitStatus() = mIsInitSuccess.get()

    fun getBannerObservable() = appodealObservable.filter {
        it.type == AppodealEvent.COMMON_TYPE
                && it.type == AppodealEvent.BANNER_VIEW_TYPE
    }

    fun getInterstitialObservable() = appodealObservable.filter {
        it.type == AppodealEvent.COMMON_TYPE
                && it.type == AppodealEvent.INTERSTITIAL_TYPE
    }

    fun addBannerCallback(callback: IBanner) {
        getBannerObservable().subscribe(shortSubscription {
            it?.let {
                when (it.event) {
                    AppodealEvent.BANNER_LOADED -> callback.onBannerLoaded(it.extra
                            .getInt(AppodealEvent.HEIGHT), it.extra.getBoolean(AppodealEvent.IS_PRECACHE))
                    AppodealEvent.BANNER_FAILED_TO_LOAD -> callback.onBannerFailedToLoad()
                    AppodealEvent.BANNER_SHOWN -> callback.onBannerShown()
                    AppodealEvent.BANNER_CLICKED -> callback.onBannerClicked()
                }
            }
        })
    }

}