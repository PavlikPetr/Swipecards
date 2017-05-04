package com.topface.topface.api.responses

import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.fragments.feed.enhanced.chat.IChatItem
import com.topface.topface.utils.Utils.EMPTY
import java.util.*

/**
 * Топливо для скраффи
 * Created by tiberal on 06.03.17.
 */
data class DevTestDataResponse(val version: Int)

data class Completed(val completed: Boolean)

data class Balance(val premium: Boolean, val likes: Int, val money: Int)

data class City(val id: Int, val name: String, val full: String)

data class State(val online: Boolean, val deviceType: Int)

data class Photo(val id: Int, val liked: Int, val added: Long, val canBecomeLeader: Boolean,
                 val position: Int, val links: HashMap<String, String>)

data class User(val id: Long, val firstName: String, val age: Int, val sex: Int, val online: Boolean,
                val state: State, val city: City, val photo: Photo, val premium: Boolean,
                val background: Int, val banned: Boolean, val deleted: Boolean, val inBlacklist: Boolean,
                val photos: List<Photo>, val photosCount: Int, val status: String, val distance: Int)

open class HistoryItem(val text: String = EMPTY, val latitude: Float = 0f, val longitude: Float = 0f,
                       val type: Int = 0, val id: Int = 0, val created: Long = 0L, val target: Int = 0,
                       val unread: Boolean = false, val link: String? = null): IChatItem {

    override fun getItemType() = if(target == FeedDialog.OUTPUT_USER_MESSAGE) {
            // owner items
            when(type) {
                FeedDialog.DIVIDER -> DIVIDER
                FeedDialog.GIFT -> USER_GIFT
                else -> USER_MESSAGE
            }
        } else {
            // friend items
            when(type) {
                FeedDialog.DIVIDER -> DIVIDER
                FeedDialog.GIFT -> FRIEND_GIFT
                else -> FRIEND_MESSAGE
            }
        }

    companion object {
        // different messages in chat
        const val USER_MESSAGE = 1
        const val USER_GIFT = 2
        const val FRIEND_MESSAGE = 3
        const val FRIEND_GIFT = 4
        const val DIVIDER = 5
        // stubs for chat, better start numbers from 1000
        const val STUB_FEED_USER = 1001
        const val STUB_CHAT_LOADER = 1002
        const val STUB_BUY_VIP = 1003
        const val STUB_MUTUAL = 1004
    }
}

data class History(val unread: Int, val more: Boolean, val isSuspiciousUser: Boolean, val user: User,
                   val items: ArrayList<HistoryItem>)

/**
 *   id String так как сервер может прислать "1487110175:110148795" и все упадет. Плохие они.
 *   Но с новой версии обещают исправиться и все ок должно быть
 */

class Visitor : FeedItem()

class FeedBookmark : FeedItem()

/**
 * С помощью этой штуки мы можем получить данные из ответа не страдая от некорректных женериков.
 */
open class IBaseFeedResponse {
    val more: Boolean = false
    open fun getItemsList(): ArrayList<out FeedItem> = ArrayList()
}

data class GetVisitorsListResponse(val items: ArrayList<Visitor> = ArrayList()) : IBaseFeedResponse() {
    override fun getItemsList() = items
}

data class GetFeedBookmarkListResponse(val items: ArrayList<FeedBookmark> = ArrayList()) : IBaseFeedResponse() {
    override fun getItemsList() = items
}


