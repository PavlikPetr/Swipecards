package com.topface.topface.api.responses

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.Parcel
import android.os.Parcelable
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.fragments.feed.enhanced.chat.IChatItem
import com.topface.topface.ui.fragments.feed.enhanced.chat.items.IAvatarVisible
import com.topface.topface.ui.fragments.feed.enhanced.chat.items.IDivider
import com.topface.topface.ui.fragments.feed.enhanced.chat.items.IResendableItem
import com.topface.topface.utils.Utils
import com.topface.topface.utils.Utils.EMPTY
import com.topface.topface.utils.extensions.readBoolean
import com.topface.topface.utils.extensions.writeBoolean
import java.util.*

/**
 * Топливо для скраффи
 * Created by tiberal on 06.03.17.
 */
data class DevTestDataResponse(val version: Int)

data class Completed(val completed: Boolean)

data class DeleteComplete(val completed: Boolean, val items: ArrayList<Int>)

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
                       val unread: Boolean = false, val link: String? = null) : IChatItem, Parcelable,
        IAvatarVisible, IDivider, IResendableItem {
    override val isErrorVisible = ObservableBoolean(false)
    override val isSending = ObservableBoolean(false)
    override val isRetrierVisible = ObservableBoolean(false)

    override val isAvatarVisible = ObservableBoolean(false)
    override val dividerText = ObservableField(Utils.EMPTY)
    override val isDividerVisible = ObservableBoolean(false)

    override fun getItemType() = if (target == FeedDialog.OUTPUT_USER_MESSAGE) {
        // owner items
        when (type) {
            FeedDialog.GIFT -> USER_GIFT
            else -> USER_MESSAGE
        }
    } else {
        // friend items
        when (type) {
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
        // stubs for chat, better start numbers from 1000
        const val STUB_FEED_USER = 1001
        const val STUB_CHAT_LOADER = 1002
        const val STUB_BUY_VIP = 1003
        const val STUB_MUTUAL = 1004
        const val NOT_MUTUAL_BUY_VIP_STUB_MUTUAL = 1005

        @JvmField val CREATOR: Parcelable.Creator<HistoryItem> = object : Parcelable.Creator<HistoryItem> {
            override fun createFromParcel(source: Parcel): HistoryItem = HistoryItem(source)
            override fun newArray(size: Int): Array<HistoryItem?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readFloat(),
            source.readFloat(),
            source.readInt(),
            source.readInt(),
            source.readLong(),
            source.readInt(),
            source.readBoolean(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) =
            dest?.let {
                it.writeString(text)
                it.writeFloat(latitude)
                it.writeFloat(longitude)
                it.writeInt(type)
                it.writeInt(id)
                it.writeLong(created)
                it.writeInt(target)
                it.writeBoolean(unread)
                it.writeString(link)
            } ?: Unit
}

fun HistoryItem?.isFriendItem() = this != null && (this.getItemType() == HistoryItem.FRIEND_MESSAGE || this.getItemType() == HistoryItem.FRIEND_GIFT)

data class History(val unread: Int, val more: Boolean, val isSuspiciousUser: Boolean, val user: User,
                   val items: ArrayList<HistoryItem>, val mutualTime: Int)

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

data class OfferwallPlace(val type: String, val name: String)
data class OfferwallWithPlaces(val name: String? = "", private val places: List<OfferwallPlace>? = listOf(),
                               private val leftMenu: List<String>? = listOf(),
                               private val purchaseScreen: List<String>? = listOf(),
                               private val purchaseScreenVip: List<String>? = listOf()) {

    fun getPlaces(): List<OfferwallPlace> = places ?: listOf()
    fun getLeftMenu(): List<String> = leftMenu ?: listOf()
    fun getPurchaseScreen(): List<String> = purchaseScreen ?: listOf()
    fun getPurchaseScreenVip(): List<String> = purchaseScreenVip ?: listOf()
}