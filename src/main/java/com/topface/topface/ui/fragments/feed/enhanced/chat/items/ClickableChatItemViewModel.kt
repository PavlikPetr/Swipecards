package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.chat.ChatComponent
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator

/**
 * Базовая модель для итемов чата, с поддержкой лонгтапов и тапов по аватарке
 */
open class ClickableChatItemViewModel(val item: HistoryItem, val itemPosition: Int) : IDivider {
    val feedNavigator: FeedNavigator by lazy { ComponentManager.obtainComponent(ChatComponent::class.java).feedNavigator() }
    override val dividerText = item.dividerText
    override val isDividerVisible = item.isDividerVisible

    fun onLongClick(): Boolean {
        feedNavigator.showChatPopupMenu(item, itemPosition)
        return true
    }

    fun onAvatarClick() {}
}