package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики чата
 *
 */
@GenerateStatistics
object ChatStatistics {

//  названия для срезов
    const val NEW_DIALOG = "new"
    const val DIALOG_TYPE = "plc"

    /**
     *  нажатие на кнопку покупки випа из чата с популярным пользователем где 35
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_STUB_BUY_VIP_BTN = "mobile_rate_popup_show"

    /**
     *  нажатие на кнопку покупки випа из чата с популярным пользователем где заблочен чат 36
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_STUB_POPUP_VIP_BTN = "mobile_rate_popup_show"

    /**
     * Первое отравленное сообщение или подарок
     * срезы - с какого экрана пришли
     */
    @SendNow(withSlices = true)
    const val CHAT_FIRST_MESSAGE_SEND = "mobile_rate_popup_show"

    /**
     * Открытие чатика
     * срезы - откуда пришел
     */
    @SendNow(withSlices = true)
    const val CHAT_SHOW = "mobile_rate_popup_show"

    /**
     *  заход в залоченный чат где есть эксперимент с популярным пользователем (35,36)
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_SHOW = "mobile_rate_popup_show"

    /**
     *  это вход в залоченый диалог если собеседник не вип, в диалоге не было переписки, и не было входящей симпатии (No_MUTUAL_NO_VIP_STUB)
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_SHOW_NO_VIP_NO_MUTUAL = "mobile_rate_popup_show"
}