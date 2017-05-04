package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableField
import com.topface.topface.api.responses.HistoryItem

class FriendMessageViewModel(item: HistoryItem) : BaseMessageViewModel(item) {
    val text = ObservableField(item.text)
}