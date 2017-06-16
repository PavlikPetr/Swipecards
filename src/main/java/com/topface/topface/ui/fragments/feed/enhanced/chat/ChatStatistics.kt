package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Ключи для отправки статистики чата
 *
 */
@GenerateStatistics
object ChatStatistics {


//    todo ПРОВЕРИТЬ СТРОКИ, КОТОРЫЕ ОТПРАВЛЯЕМ, ИБО НАСТЯ ХУЙНЮ В ТАСКЕ НАПИСАЛА

    //  названия для срезов
    const val SOME_SLICE_KEY_FOR_GIFTS = "some_slice_key_for_gift"
    const val SOME_SLICE_WHERE_DID_YOU_COME_IN_MY_CHAT = "where_did_you_come_in_my_chat"

    /**
     *  нажатие на кнопку покупки випа из чата с популярным пользователем где 35
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_STUB_BUY_VIP_BTN = "chat_block_stub_buy_vip_btn"

    /**
     *  нажатие на кнопку покупки випа из чата с популярным пользователем где заблочен чат 36
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_STUB_POPUP_VIP_BTN = "chat_block_popup_buy_vip_btn"

    /**
     * Первое отравленное сообщение или подарок
     * срезы - с какого экрана пришли
     */
    @SendNow(withSlices = true)
    const val CHAT_FIRST_MESSAGE_SEND = "dialog_first_messages_sent"

    /**
     * Открытие чатика
     * срезы - откуда пришел
     */
    @SendNow(withSlices = true)
    const val CHAT_SHOW = "chat_show_from"

    /**
     * Открытие экрана с подарками из чатика
     */
    @SendNow(withSlices = true)
    const val CHAT_GIFT_ACTIVITY_OPEN = "mobile_gifts_open"

    /**
     *  заход в залоченный чат где есть эксперимент с популярным пользователем (35,36)
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_SHOW = "mobile_dialogs_block_messages_without_mutual"

    /**
     *  это вход в залоченый диалог если собеседник не вип, в диалоге не было переписки, и не было входящей симпатии (No_MUTUAL_NO_VIP_STUB)
     */
    @SendNow(withSlices = false)
    const val CHAT_BLOCK_SHOW_NO_VIP_NO_MUTUAL = "mobile_dialogs_block_messages_without_mutual_without_vip"
}