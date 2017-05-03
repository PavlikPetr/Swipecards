package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.content.Intent
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Photo
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatActivity.Companion.REQUEST_CHAT


object ChatIntentCreator {

    const val INTENT_AVATAR = "user_avatar"
    const val INTENT_USER_ID = "user_id"
    const val INTENT_USER_CITY = "user_city"
    const val INTENT_USER_NAME_AND_AGE = "user_name_and_age"
    const val INTENT_ITEM_ID = "item_id"
    const val GIFT_DATA = "gift_data"
    const val BANNED_USER = "banned_user"
    const val SEX = "sex"
    const val IN_BLACKLIST = "in_blacklist"
    const val IS_BOOKMARKED = "is_bookmarked"
    const val ONLINE = "online"
    const val USER_TYPE = "type"
    const val WHOLE_USER = "whole_user"

    fun createIntentForChatFromDating(user: FeedUser, answer: SendGiftAnswer?) =
            if (isOldChat()) {
                ChatIntentCreator.createIntent(user.id, user.sex, user.nameAndAge, user.city.name, null,
                        user.photo, false, answer, user.inBlacklist, user.bookmarked, user.banned, user.online)
            } else {
                createIntent(user, answer)
            }


    @JvmStatic
    fun createIntent(user: FeedUser, answer: SendGiftAnswer?) = Intent(App.getContext(),
            com.topface.topface.ui.fragments.feed.enhanced.chat.ChatActivity::class.java).apply {
        putExtra(WHOLE_USER, user)
        putExtra(GIFT_DATA, answer)
    }

    //Если itemType соответствует популярному юзеру не показываем клаву в чате
    @JvmStatic
    @JvmOverloads
    fun createIntent(id: Int, sex: Int, nameAndAge: String, city: String, feedItemId: String?,
                     photo: Photo?, fromGcm: Boolean, itemType: Int, isBanned: Boolean,
                     inBlacklist: Boolean, isBookmarked: Boolean, isOnline: Boolean = false): Intent {
        return createIntent(id, sex, nameAndAge, city, feedItemId, photo, fromGcm, null, isBanned,
                inBlacklist, isBookmarked, isOnline).putExtra(USER_TYPE, itemType)
    }

    @JvmStatic
    @JvmOverloads
    fun createIntent(id: Int, sex: Int, nameAndAge: String, city: String, feedItemId: String?,
                     photo: Photo?, fromGcm: Boolean, answer: SendGiftAnswer?, isBanned: Boolean,
                     inBlacklist: Boolean, isBookmarked: Boolean, isOnline: Boolean = false): Intent {
        val intent = Intent(App.getContext(), getChatClass())
        intent.putExtra(INTENT_USER_ID, id)
        intent.putExtra(INTENT_USER_NAME_AND_AGE, nameAndAge)
        intent.putExtra(INTENT_USER_CITY, city)
        intent.putExtra(GIFT_DATA, answer)
        intent.putExtra(SEX, sex)
        intent.putExtra(BANNED_USER, isBanned)
        intent.putExtra(ONLINE, isOnline)
        intent.putExtra(IN_BLACKLIST, inBlacklist)
        intent.putExtra(IS_BOOKMARKED, isBookmarked)
        if (!TextUtils.isEmpty(feedItemId)) {
            intent.putExtra(INTENT_ITEM_ID, feedItemId)
        }
        if (fromGcm) {
            intent.putExtra(App.INTENT_REQUEST_KEY, REQUEST_CHAT)
        }
        photo?.let { intent.putExtra(INTENT_AVATAR, it) }
        return intent
    }

    private fun getChatClass() = com.topface.topface.ui.ChatActivity::class.java
//    private fun getChatClass() = if (isOldChat()) {
//        com.topface.topface.ui.ChatActivity::class.java
//    } else {
//        com.topface.topface.ui.fragments.feed.enhanced.chat.ChatActivity::class.java
//    }

    private fun isOldChat() = false

}
