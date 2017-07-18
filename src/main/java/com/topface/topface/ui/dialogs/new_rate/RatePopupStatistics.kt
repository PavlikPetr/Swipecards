package com.topface.topface.ui.dialogs.new_rate

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики оценки приложения
 *
 */
@GenerateStatistics
object RatePopupStatistics {

    const val NEW_DIALOG = "new"
    const val DIALOG_TYPE = "plc"

    /**
     * показы попапа оценки приложения
     */
    @SendNow(withSlices = true)
    const val RATE_POPUP_SHOW = "mobile_rate_popup_show"

    /**
     *  закрытие попапа оценки приложения
     */
    @SendNow(withSlices = true)
    const val RATE_POPUP_CLOSE = "mobile_rate_popup_close"

    /**
     * Клик по кнопке закрытия попапа оценки приложения
     */
    @SendNow(withSlices = true)
    const val RATE_POPUP_CLICK_BUTTON_CLOSE = "mobile_rate_popup_click_button_close"

    /**
     * Клик по кнопке отправить отзыв
     */
    @SendNow(withSlices = true)
    const val RATE_POPUP_CLICK_BUTTON_RATE = "mobile_rate_popup_click_button_rate"

}