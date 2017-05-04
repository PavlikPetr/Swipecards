package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.api.responses.HistoryItem

class FriendMessageViewModel(item: HistoryItem, longClickListener: () -> Boolean = { false }, avatarClickListener: () -> Unit = {})
    : BaseMessageViewModel(item, longClickListener, avatarClickListener)