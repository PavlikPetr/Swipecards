package com.topface.topface.banners

import com.topface.topface.App
import com.topface.topface.banners.providers.appodeal.AppodealProvider
import com.topface.topface.data.AdsSettings
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.RunningStateManager
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Created by ppavlik on 31.05.17.
 * Контроллер который при инициализации отправляет запрос для получения настроек показа баннероной рекламы
 * и вызывает ее загрузку в контейнер
 * Новые застройки будут получены при возвращении пользователя на экран (свернул апу - вернулся, открыл чат/профиль - вернулся по back/up)
 */
class BannersController(private var mPage: IBannerAds) : ILifeCycle, RunningStateManager.OnAppChangeStateListener {

    private val mFeedBannersInjector by lazy {
        BannerInjector()
    }

    private val mProvidersFactory by lazy {
        AdProvidersFactory()
    }

    private val mRunningStateManager by lazy {
        App.getAppComponent().runningStateManager()
    }

    private val mApi by lazy {
        App.getAppComponent().api()
    }

    private val mUserConfig by lazy {
        App.getUserConfig()
    }

    private val mWeakStorage by lazy {
        App.getAppComponent().weakStorage()
    }

    private var mBannerSubscription: Subscription? = null

    init {
        sendSettingsRequest()
    }

    override fun onAppForeground(timeOnStart: Long) = sendSettingsRequest()

    override fun onAppBackground(timeOnStop: Long, timeOnStart: Long) {
        mBannerSubscription?.safeUnsubscribe()
    }

    fun release() {
        mFeedBannersInjector.cleanUp()
    }

    override fun onResume() {
        super.onResume()
        mRunningStateManager.registerAppChangeStateListener(this)
    }

    private fun sendSettingsRequest() {
        mBannerSubscription = mApi.callBannerGetCommon()
                .subscribe(shortSubscription {
                    it?.let { response ->
                        if (response.banner.type == AdsSettings.SDK) {
                            mUserConfig.setBannerInterval(response.nextRequestNoEarlierThen)
                            when (response.banner.name) {
                                AdProvidersFactory.BANNER_APPODEAL -> {
                                    mWeakStorage.appodealBannerSegmentName = response.banner.adAppId
                                    AppodealProvider.setCustomSegment()
                                }
                                AdProvidersFactory.BANNER_AMPIRI -> {
                                    mWeakStorage.ampiriBannerSegmentName = response.banner.adAppId
                                }
                            }
                            mProvidersFactory.createProvider(response.banner.name)?.let {
                                mFeedBannersInjector.injectBanner(response.banner.name, it, mPage)
                            }
                        }
                    }
                })
    }

    override fun onPause() {
        super.onPause()
//         // TODO НИЖЕ ГОВНО ПОПРАВЬ ПАРЯ
//      ниже закомичен метод cleanUp(), чтобы краша не было...
//      лог и кейсы описаны тут - https://tasks.verumnets.ru/issues/57082

//        mFeedBannersInjector.cleanUp()
        mRunningStateManager.unregisterAppChangeStateListener(this)
    }
}