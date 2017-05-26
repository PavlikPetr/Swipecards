package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.api.responses.HistoryItem.Companion.NOT_MUTUAL_BUY_VIP_STUB_MUTUAL
import com.topface.topface.api.responses.HistoryItem.Companion.STUB_BUY_VIP
import com.topface.topface.api.responses.HistoryItem.Companion.STUB_CHAT_LOADER
import com.topface.topface.api.responses.HistoryItem.Companion.STUB_MUTUAL

/**
 * Модельки для чата
 */

/**
 * Лоадер на время отправки запроса
 */
data class ChatLoader(var diffTemp: Int = 0) : IChatItem {
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
class UserGift(item: HistoryItem) :
        HistoryItem(item.text, item.latitude, item.longitude, item.type,
                item.id, item.created, item.target, item.unread, item.link)

/**
 * итем чата - подарок от собеседника
 */
class FriendGift(item: HistoryItem) :
        HistoryItem(item.text, item.latitude, item.longitude, item.type,
                item.id, item.created, item.target, item.unread, item.link)

/**
 * итем чата - сообщение от пользователя
 */
class UserMessage(item: HistoryItem) :
        HistoryItem(item.text, item.latitude, item.longitude, item.type,
                item.id, item.created, item.target, item.unread, item.link)

/**
 * итем чата - сообщение от собеседника
 */
class FriendMessage(item: HistoryItem) :
        HistoryItem(item.text, item.latitude, item.longitude, item.type,
                item.id, item.created, item.target, item.unread, item.link)

/**
 * заглушка чата про взаимные симпатии
 */
class MutualStub : IChatItem {
    override fun getItemType() = STUB_MUTUAL
}

/**
 * заглушка чата "Нет випа, нет взаимного, но хочешь написать, подлец"
 */
class NotMutualBuyVipStub : IChatItem {
    override fun getItemType() = NOT_MUTUAL_BUY_VIP_STUB_MUTUAL
}

/**
 * заглушка чата про покупку вип
 */
class BuyVipStub : IChatItem {
    override fun getItemType() = STUB_BUY_VIP
}

/**
 * интерфейс макирующий итемы чата, в частности для разбора базового HistoryItem
 */
interface IChatItem {
    fun getItemType(): Int
}