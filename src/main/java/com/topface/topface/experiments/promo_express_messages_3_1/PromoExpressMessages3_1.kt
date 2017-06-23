package com.topface.topface.experiments.promo_express_messages_3_1

import com.topface.topface.App
import com.topface.topface.data.Options

/**
 * Logic for promo express popup 3-1 experiment
 */
object PromoExpressMessages3_1 {
    fun storeViewedPopupID() = with(App.getUserConfig()) {
        getCustomPopupSettings()?.customPopupId?.let { setPopup3_1ViewedIds(popup3_1viewedIds.apply { add(it) }) }
    }

    fun isIdStored(id: String) = with(App.getUserConfig()) {
        popup3_1viewedIds.contains(id)
    }

    /**
     * Return not NULL only if custom popup can be shown
     */
    fun getCustomPopupSettings(): Options.PromoPopupEntity? = with(App.get().options.premiumMessages) {
        if (isCustomPopupEnabled && !isIdStored(customPopupId)) {
            return@with this
        }
        return null
    }

    fun getStatisticPlc(): String? = getCustomPopupSettings()?.let { return it.customPopupId }
}