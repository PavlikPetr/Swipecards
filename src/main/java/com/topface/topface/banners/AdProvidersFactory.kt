package com.topface.topface.banners

import com.topface.topface.banners.providers.AmpiriProvider
import com.topface.topface.banners.providers.appodeal.AppodealProvider

/**
 * Created by ppavlik on 01.06.17.
 * Factory through which you can obtain needed ad's provider
 */
class AdProvidersFactory {
    companion object {
        /**
         * Идентификаторы типов баннеров
         */
        const val  BANNER_NONE = "NONE"
        const val BANNER_APPODEAL = "APPODEAL"
        const val BANNER_AMPIRI = "AMPIRI"
    }

    fun createProvider(banner: String) = when (banner) {
        BANNER_APPODEAL -> AppodealProvider()
        BANNER_AMPIRI -> AmpiriProvider()
        else -> null
    }
}