package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableField
import com.topface.topface.api.responses.HistoryItem

open class BaseMessageViewModel(item: HistoryItem, itemPosition: Int) : ClickableChatItemViewModel(item, itemPosition) {
    val text = ObservableField(item.text)
}