package com.topface.topface.ui.external_libs.ironSource

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Created by ppavlik on 13.06.17.
 * лючи для отправки статистики по оферволам от IRonSource
 */

@GenerateStatistics
object IronSourceStatistics {

    const val LEFT_MENU_PLC = "leftMenu"
    const val PURCHASES_TAB_PLC = "purchasesTab"
    const val EXTRA_ACTIVITY_PLC = "extraActivity"
    const val BUY_VIP_PLC = "buyVip"
    const val BUY_LIKES_COINS_PLC = "buyLikesCoins"

    /**
     * Вызов показа офервола
     * срез plc с типом офера
     */
    @SendNow(withSlices = true)
    const val IRON_SOURCE_SHOW_OFFERS = "offer_shown"
}