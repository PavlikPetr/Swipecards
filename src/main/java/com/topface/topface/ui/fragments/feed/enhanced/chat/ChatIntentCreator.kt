package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.content.Intent
import android.text.TextUtils

import com.topface.topface.App
import com.topface.topface.data.Photo
import com.topface.topface.data.SendGiftAnswer

import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatActivity.Companion.REQUEST_CHAT


object ChatIntentCreator {

    //Если itemType соответствует популярному юзеру не показываем клаву в чате
    @JvmStatic
    fun createIntent(id: Int, sex: Int, nameAndAge: String, city: String, feedItemId: String?, photo: Photo, fromGcm: Boolean, itemType: Int, isBanned: Boolean): Intent {
        return createIntent(id, sex, nameAndAge, city, feedItemId, photo, fromGcm, null, isBanned).putExtra(ChatFragment.USER_TYPE, itemType)
    }

    @JvmStatic
    fun createIntent(id: Int, sex: Int, nameAndAge: String, city: String, feedItemId: String?, photo: Photo?, fromGcm: Boolean, answer: SendGiftAnswer?, isBanned: Boolean): Intent {
        val intent = Intent(App.getContext(), getChatClass())
        intent.putExtra(ChatFragment.INTENT_USER_ID, id)
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, nameAndAge)
        intent.putExtra(ChatFragment.INTENT_USER_CITY, city)
        intent.putExtra(ChatFragment.GIFT_DATA, answer)
        intent.putExtra(ChatFragment.SEX, sex)
        intent.putExtra(ChatFragment.BANNED_USER, isBanned)
        if (!TextUtils.isEmpty(feedItemId)) {
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, feedItemId)
        }
        if (fromGcm) {
            intent.putExtra(App.INTENT_REQUEST_KEY, REQUEST_CHAT)
        }
        if (photo != null) {
            intent.putExtra(ChatFragment.INTENT_AVATAR, photo)
        }
        return intent
    }

    private fun getChatClass() = if (isOldChat()) {
        com.topface.topface.ui.ChatActivity::class.java
    } else {
        com.topface.topface.ui.fragments.feed.enhanced.chat.ChatActivity::class.java
    }

    private fun isOldChat() = false

}
