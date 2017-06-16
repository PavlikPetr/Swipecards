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
    const val START_CHAT_FROM = "plc"

    /**
     *  нажатие на кнопку покупки випа из чата с популярным пользователем где 35
     */
    @SendNow
    const val CHAT_BLOCK_STUB_BUY_VIP_BTN = "chat_block_stub_buy_vip_btn"

    /**
     *  нажатие на кнопку покупки випа из чата с популярным пользователем где заблочен чат 36
     */
    @SendNow
    const val CHAT_BLOCK_STUB_POPUP_VIP_BTN = "chat_block_popup_buy_vip_btn"

    /**
     * Первое отравленное сообщение или подарок
     * срезы - с какого экрана пришли
     */
    @SendNow(withSlices = true)
    const val CHAT_FIRST_MESSAGE_SEND = "dialog_first_messages_sent"

    /**
     * Открытие чатика. Строка действительно такая кривая. В Вики все указано
     * срезы - с какого экрана пришли
     */
    @SendNow(withSlices = true)
    const val CHAT_SHOW = "mobile_profile_open"

    /**
     * Открытие экрана с подарками из чатика
     * срез - из чатика
     */
    @SendNow(withSlices = true)
    const val CHAT_GIFT_ACTIVITY_OPEN = "mobile_gifts_open"

    /**
     *  заход в залоченный чат где есть эксперимент с популярным пользователем (35,36)
     */
    @SendNow
    const val CHAT_BLOCK_SHOW = "mobile_dialogs_block_messages_without_mutual"

    /**
     *  это вход в залоченый диалог если собеседник не вип, в диалоге не было переписки, и не было входящей симпатии (No_MUTUAL_NO_VIP_STUB)
     */
    @SendNow
    const val CHAT_BLOCK_SHOW_NO_VIP_NO_MUTUAL = "mobile_dialogs_block_messages_without_mutual"
}