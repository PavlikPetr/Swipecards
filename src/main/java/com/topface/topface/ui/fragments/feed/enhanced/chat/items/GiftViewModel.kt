package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableField
import com.topface.topface.api.responses.HistoryItem

class GiftViewModel(item: HistoryItem, itemPosition: Int) : ClickableChatItemViewModel(item, itemPosition) {
    val link = ObservableField(item.link)
}