package com.topface.topface.statistics

import android.os.Bundle
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.topface.topface.App
import com.topface.topface.utils.social.FbAuthorizer
import java.math.BigDecimal
import java.util.*

/**
 * Sends statistic to Facebook
 */
object FBStatistics {
    // own custom event, must send when trial vip period started
    const val VIP_TRIAL_STARTED_EVENT = "fb_vip_trial_started"

    const val PLACE_PURCHASE_VIP = "PURCHASE_VIP"
    const val PLACE_PURCHASE_COINS = "PURCHASE_COINS"
    const val PLACE_POPUP_VIP_TRIAL = "POPUP_VIP_TRIAL"

    val logger: AppEventsLogger by lazy {
        AppEventsLogger.newLogger(App.getContext())
    }

    fun onVipTrialStarted() {
        FbAuthorizer.initFB()
        logger.logEvent(VIP_TRIAL_STARTED_EVENT)
    }

    fun onContentViewed(place: String?) {
        FbAuthorizer.initFB()
        logger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "PLACE_${place?.toUpperCase()}")
        })
    }

    fun onPurchase(price: Double, currencyCode: String, bundle: Bundle?) {
        FbAuthorizer.initFB()
        logger.logPurchase(BigDecimal.valueOf(price), Currency.getInstance(currencyCode), bundle)
    }
}