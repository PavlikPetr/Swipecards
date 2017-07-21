package com.topface.topface.ui.external_libs.appodeal

import android.app.Activity
import android.support.annotation.IntDef
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.utils.Log
import com.topface.framework.utils.Debug
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Emitter
import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by ppavlik on 03.07.17.
 * Менеджер для работы с sdk Appodeal
 */
class AppodealManager {

    companion object {
        val APPODEAL_APP_KEY = "2f48418b677cf24a3fa37eacfc7a4e76d385db08b51bd328"
        private const val TAG = "AppodealManager"

        const val NOT_INITED = 0L
        private const val INIT_IN_PROGRESS = 1L
        private const val INITED_SUCCESS = 2L

        @IntDef(NOT_INITED, INIT_IN_PROGRESS, INITED_SUCCESS)
        annotation class AppodealInitState
    }

    val appodealObservable: Observable<AppodealEvent>

    private val mIsInitSuccess = AtomicLong(NOT_INITED)
    private var mBannerState = AppodealEvent.UNDEFINED_EVENT
    private var mInterstitialState = AppodealEvent.UNDEFINED_EVENT
    private val mAllSubscription = CompositeSubscription()
    private var mShowBanner: (() -> Unit)? = null
    private var mShowFullscreen: (() -> Unit)? = null
    private var mEmitter: Emitter<AppodealEvent>? = null
    private val mCallbackMap = hashMapOf<Int, Subscription>()

    init {
        appodealObservable = Observable.fromEmitter<AppodealEvent>({
            mEmitter = it
        }, Emitter.BackpressureMode.LATEST).share()
        Appodeal.setBannerCallbacks(object : BannerCallbacks {
            override fun onBannerShown() {
                Debug.log("$TAG onBannerShown")
                mEmitter?.onNext(AppodealEvent.onBannerShown())
            }

            override fun onBannerLoaded(height: Int, isPrecashe: Boolean) {
                Debug.log("$TAG onBannerLoaded height:$height isPrecashe:$isPrecashe")
                mEmitter?.onNext(AppodealEvent.onBannerLoaded(height, isPrecashe))
            }

            override fun onBannerClicked() {
                Debug.log("$TAG onBannerClicked")
                mEmitter?.onNext(AppodealEvent.onBannerClicked())
            }

            override fun onBannerFailedToLoad() {
                Debug.log("$TAG onBannerFailedToLoad")
                mEmitter?.onNext(AppodealEvent.onBannerFailedToLoad())
            }
        })
        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialLoaded(isPrecashe: Boolean) {
                Debug.log("$TAG onInterstitialLoaded isPrecashe:$isPrecashe")
                mEmitter?.onNext(AppodealEvent.onInterstitialLoaded(isPrecashe))
            }

            override fun onInterstitialShown() {
                Debug.log("$TAG onInterstitialShown")
                mEmitter?.onNext(AppodealEvent.onInterstitialShown())
            }

            override fun onInterstitialClicked() {
                Debug.log("$TAG onInterstitialClicked")
                mEmitter?.onNext(AppodealEvent.onInterstitialClicked())
            }

            override fun onInterstitialFailedToLoad() {
                Debug.log("$TAG onInterstitialFailedToLoad")
                mEmitter?.onNext(AppodealEvent.onInterstitialFailedToLoad())
            }

            override fun onInterstitialClosed() {
                Debug.log("$TAG onInterstitialClosed")
                mEmitter?.onNext(AppodealEvent.onInterstitialClosed())
            }
        })
        mAllSubscription.add(appodealObservable.filter { it.type == AppodealEvent.COMMON_TYPE }
                .take(2)
                .subscribe(shortSubscription {
                    it?.let {
                        when (it.event) {
                            AppodealEvent.SDK_START_INIT -> {
                                Debug.log("$TAG onSdkStartInit")
                                mIsInitSuccess.set(INIT_IN_PROGRESS)
                            }
                            AppodealEvent.SDK_INIT_SUCCESFULL -> {
                                Debug.log("$TAG onSdkInitSuccesfull")
                                mIsInitSuccess.set(INITED_SUCCESS)
                            }
                        }
                    }
                }))
        mAllSubscription.add(getBannerObservable().subscribe(shortSubscription {
            it?.let {
                mBannerState = it.event
                if (mBannerState == AppodealEvent.BANNER_LOADED) {
                    mShowBanner?.let {
                        Debug.log("$TAG Try to start the pending banner show action")
                        it.invoke()
                        mShowBanner = null
                    }
                }
            }
        }))
        mAllSubscription.add(getInterstitialObservable().subscribe(shortSubscription {
            it?.let {
                mInterstitialState = it.event
                if (mInterstitialState == AppodealEvent.INTERSTITIAL_LOADED) {
                    mShowFullscreen?.let {
                        Debug.log("$TAG Try to start the pending interstitial show action")
                        it.invoke()
                        mShowFullscreen = null
                    }
                }
            }
        }))
    }

    /**
     * Initialize appodeal sdk, if it wasn't initialized earlier
     *
     * @param activity activity instance
     */
    fun initAppodeal(activity: Activity) {
        if (getInitStatus() == NOT_INITED) {
            mEmitter?.onNext(AppodealEvent.onSdkStartInit())
            Appodeal.setTesting(false)
            Appodeal.setLogLevel(Log.LogLevel.verbose)
            Appodeal.setAutoCache(Appodeal.INTERSTITIAL or Appodeal.BANNER_VIEW, true)
            Appodeal.initialize(activity, APPODEAL_APP_KEY, Appodeal.INTERSTITIAL or Appodeal.BANNER_VIEW)
            mEmitter?.onNext(AppodealEvent.onSdkInitSuccesfull())
        }
    }

    /**
     * Show banner
     * We will try to initialize sdk, if it was not initialized earlier
     * Banner will only be shown after initialization
     *
     * @see hideBanner
     * @param activity activity instance
     */
    fun showBanner(activity: Activity) {
        if (mBannerState != AppodealEvent.UNDEFINED_EVENT) {
            Debug.log("$TAG show banner")
            mShowBanner = null
            Appodeal.show(activity, Appodeal.BANNER_VIEW)
        } else {
            Debug.log("$TAG show banner after initialization")
            mShowBanner = { showBanner(activity) }
            initAppodeal(activity)
        }
    }

    /**
     * Hide banner to prevent memory leaks
     *
     * @param activity activity instance
     */
    fun hideBanner(activity: Activity) {
        Debug.log("$TAG hide banner")
        Appodeal.hide(activity, Appodeal.BANNER_VIEW)
    }

    /**
     * Show interstitial
     * We will try to initialize sdk, if it was not initialized earlier
     * Interstitial will only be shown after initialization
     *
     * @param activity activity instance
     */
    fun showFullscreen(activity: Activity) {
        if (mInterstitialState != AppodealEvent.UNDEFINED_EVENT) {
            Debug.log("$TAG show interstitial")
            mShowFullscreen = null
            Appodeal.show(activity, Appodeal.INTERSTITIAL)
        } else {
            Debug.log("$TAG show interstitial after initialization")
            mShowFullscreen = { showFullscreen(activity) }
            initAppodeal(activity)
        }
    }

    /**
     * Return true if appodeal sdk initialized successfull
     *
     * @return true if appodeal sdk initialized successfull
     */
    fun isInitSuccess() = mIsInitSuccess.get() == INITED_SUCCESS

    /**
     * Get the current status of sdk initialization
     *
     * @see AppodealInitState
     * @return initialization status
     */
    @AppodealInitState
    fun getInitStatus() = mIsInitSuccess.get()

    /**
     * Get an observer for the events of banner
     *
     * @return observer of banner events
     */
    fun getBannerObservable(): Observable<AppodealEvent> = appodealObservable.filter {
        it.type == AppodealEvent.COMMON_TYPE
                || it.type == AppodealEvent.BANNER_VIEW_TYPE
    }

    /**
     * Get an observer for the events of interstitial
     *
     * @return observer of interstitial events
     */
    fun getInterstitialObservable(): Observable<AppodealEvent> = appodealObservable.filter {
        it.type == AppodealEvent.COMMON_TYPE
                || it.type == AppodealEvent.INTERSTITIAL_TYPE
    }

    /**
     * Add banner callback
     *
     * @param callback Listener to notify when banner events occur.
     * @see removeBannerCallback
     */
    fun addBannerCallback(callback: IBanner) {
        mCallbackMap.put(callback.hashCode(), getBannerObservable()
                .subscribe(shortSubscription {
                    it?.let {
                        when (it.event) {
                            AppodealEvent.BANNER_LOADED -> callback.onBannerLoaded(it.extra
                                    .getInt(AppodealEvent.HEIGHT), it.extra.getBoolean(AppodealEvent.IS_PRECACHE))
                            AppodealEvent.BANNER_FAILED_TO_LOAD -> callback.onBannerFailedToLoad()
                            AppodealEvent.BANNER_SHOWN -> callback.onBannerShown()
                            AppodealEvent.BANNER_CLICKED -> callback.onBannerClicked()
                            AppodealEvent.SDK_INIT_SUCCESFULL -> callback.initSuccessfull()
                            AppodealEvent.SDK_START_INIT -> callback.startInit()
                        }
                    }
                }))
    }

    /**
     * Removes the specified listener from the map of listeners that will be notified of banner
     * events.
     *
     * @param callback Listener to remove from being notified of banner events
     * @see addBannerCallback
     */
    fun removeBannerCallback(callback: IBanner) {
        removeCallback(callback.hashCode())
    }

    /**
     * Add interstitial callback
     *
     * @param callback Listener to notify when interstitial events occur.
     * @see removeInterstitialCallback
     */
    fun addInterstitialCallback(callback: IFullscreen) {
        mCallbackMap.put(callback.hashCode(), getInterstitialObservable()
                .subscribe(shortSubscription {
                    it?.let {
                        when (it.event) {
                            AppodealEvent.SDK_INIT_SUCCESFULL -> callback.initSuccessfull()
                            AppodealEvent.SDK_START_INIT -> callback.startInit()
                            AppodealEvent.INTERSTITIAL_LOADED -> callback.onInterstitialLoaded(it.extra.getBoolean(AppodealEvent.IS_PRECACHE))
                            AppodealEvent.INTERSTITIAL_FAILED_TO_LOAD -> callback.onInterstitialFailedToLoad()
                            AppodealEvent.INTERSTITIAL_SHOWN -> callback.onInterstitialShown()
                            AppodealEvent.INTERSTITIAL_CLICKED -> callback.onInterstitialClicked()
                            AppodealEvent.INTERSTITIAL_CLOSED -> callback.onInterstitialClosed()

                        }
                    }
                }))
    }

    /**
     * Removes the specified listener from the map of listeners that will be notified of interstitial
     * events.
     *
     * @param callback Listener to remove from being notified of interstitial events
     * @see addInterstitialCallback
     */
    fun removeInterstitialCallback(callback: IFullscreen) {
        removeCallback(callback.hashCode())
    }

    private fun removeCallback(hashCode: Int) {
        Debug.log("$TAG total callbacks map size before remove callback with hashCode:$hashCode = ${mCallbackMap.size}")
        mCallbackMap.remove(hashCode).run {
            Debug.log("$TAG remove subscription $this")
            safeUnsubscribe()
            Debug.log("$TAG subscription $this unsibscribed:${this?.isUnsubscribed ?: true}")
        }
        Debug.log("$TAG total callbacks map size after remove callback with hashCode:$hashCode = ${mCallbackMap.size}")
    }

    /**
     * Remove all links and subscription when manager destroy
     */
    fun release() {
        mAllSubscription.safeUnsubscribe()
        mCallbackMap.keys.forEach { removeCallback(it) }
        mShowBanner = {}
        mShowFullscreen = {}
        mEmitter = null
        mIsInitSuccess.set(NOT_INITED)
        mBannerState = AppodealEvent.UNDEFINED_EVENT
        mInterstitialState = AppodealEvent.UNDEFINED_EVENT
    }
}