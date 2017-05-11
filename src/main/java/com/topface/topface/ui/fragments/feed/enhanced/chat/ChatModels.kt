package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.api.responses.HistoryItem.Companion.STUB_BUY_VIP
import com.topface.topface.api.responses.HistoryItem.Companion.STUB_CHAT_LOADER
import com.topface.topface.api.responses.HistoryItem.Companion.STUB_MUTUAL

/**
 * Модельки для чата
 */

/**
 * Лоадер на время отправки запроса
 */
data class ChatLoader(var diffTemp: Int = 0): IChatItem {
    override fun getItemType() = STUB_CHAT_LOADER
}

/**
 * Событие на удаление
 */
data class ChatDeleteEvent(var itemPosition: Int = 0)

/**
 * Событие на жалобу
 */
data class ChatComplainEvent(var itemPosition: Int = 0)

/**
 * итем чата - подарок от пользователя
 */
class UserGift: HistoryItem()

/**
 * итем чата - подарок от собеседника
 */
class FriendGift: HistoryItem()

/**
 * итем чата - сообщение от пользователя
 */
class UserMessage: HistoryItem()

/**
 * итем чата - сообщение от собеседника
 */
class FriendMessage: HistoryItem()

/**
 * заглушка чата про взаимные симпатии
 */
class MutualStub: IChatItem {
    override fun getItemType() = STUB_MUTUAL
}

/**
 * заглушка чата про покупку вип
 */
class BuyVipStub: IChatItem {
    override fun getItemType() = STUB_BUY_VIP
}

/**
 * интерфейс макирующий итемы чата, в частности для разбора базового HistoryItem
 */
interface IChatItem {
    fun getItemType(): Int
}