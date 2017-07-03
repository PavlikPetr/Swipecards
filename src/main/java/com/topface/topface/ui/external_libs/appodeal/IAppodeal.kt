package com.topface.topface.ui.external_libs.appodeal

import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks

/**
 * Created by ppavlik on 03.07.17.
 * Интерфейсы для работы с appodeal
 */
interface ISdkInitialization {
    /**
     * Колбек о том, что начали инциализацию sdk
     */
    fun startInit()

    /**
     * Колбек о том, что инциализация sdk была завершена успешно
     */
    fun initSuccessfull()
}

interface IBanner : ISdkInitialization, BannerCallbacks

interface IFullscreen : ISdkInitialization, InterstitialCallbacks