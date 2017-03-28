package com.topface.topface.api.responses

import com.topface.topface.data.FeedItem
import java.util.*
import kotlin.collections.ArrayList

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


