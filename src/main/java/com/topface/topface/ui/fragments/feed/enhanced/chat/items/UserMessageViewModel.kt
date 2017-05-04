package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.api.responses.HistoryItem

class UserMessageViewModel(item: HistoryItem, longClickListener: () -> Boolean = { false })
    : BaseMessageViewModel(item, longClickListener = longClickListener)