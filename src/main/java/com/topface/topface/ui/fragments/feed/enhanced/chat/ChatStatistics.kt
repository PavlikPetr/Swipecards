package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики чата
 *
 */
@GenerateStatistics
object ChatStatistics {

    /**
     * Первое отравленное сообщение или подарок
     *
     */
@SendNow(withSlices = true)
        const val RATE_POPUP_SHOW = "mobile_rate_popup_show"


}
/**
 * Ключи для отправки статистики оценки приложения
 *
 */
@GenerateStatistics
object RatePopupStatistics {

    const val NEW_DIALOG = "new"
    const val DIALOG_TYPE = "plc"



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
