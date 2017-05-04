package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.chat.ChatComponent
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import javax.inject.Inject

/**
 * Базовая модель для итемов чата, с поддержкой лонгтапов и тапов по аватарке
 */
open class ClickableChatItemViewModel(val item:HistoryItem, val itemPosition:Int) {
    @Inject lateinit var feedNavigator: FeedNavigator
    init {
        ComponentManager.obtainComponent(ChatComponent::class.java).inject(this)
    }

    fun onLongClick(): Boolean {
        feedNavigator.showChatPopupMenu(item, itemPosition)
        return true
    }

    fun onAvatarClick() {}
}