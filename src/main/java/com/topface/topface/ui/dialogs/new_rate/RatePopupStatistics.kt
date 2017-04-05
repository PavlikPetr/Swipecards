package com.topface.topface.ui.dialogs.new_rate

import com.topface.statistics.android.Slices
import com.topface.statistics.android.StatisticsTracker
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики приглашений в приложение через FB
 * Created by ppavlik on 26.12.16.
 */

object RatePopupStatistics {

    /**
     * показы попапа оценки приложения
     */
    @SendNow(single = false)
    const val RATE_POPUP_SHOW = "mobile_rate_popup_show"

    /**
     *  закрытие попапа оценки приложения
     */
    @SendNow(single = false)
    const val RATE_POPUP_CLOSE = "mobile_rate_popup_close"

    /**
     * Клик по кнопке закрытия попапа оценки приложения
     */
    @SendNow(single = false)
    const val RATE_POPUP_CLICK_BUTTON_CLOSE = "mobile_rate_popup_click_button_close"

    /**
     * Клик по кнопке отправить отзыв
     */
    @SendNow(single = false, withSlices = true)
    const val RATE_POPUP_CLICK_BUTTON_RATE = "mobile_rate_popup_click_button_rate"

    /**
     * Клик по кнопке приглашения друзей через FB
     */
    const val RATING = "val"


    fun sendRatePopupClickButtonRate(rateValue: Long) {
        StatisticsTracker.getInstance().sendEvent(RATE_POPUP_CLICK_BUTTON_RATE, 1, generateSlices(rateValue))
    }

    private fun generateSlices(value: Long): Slices {
        return Slices()
                .putSlice(RATING, value.toString())
    }

}