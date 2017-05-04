package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableField
import com.topface.topface.api.responses.HistoryItem

open class BaseMessageViewModel(val item: HistoryItem, val longClickListener: () -> Boolean = { false }, val avatarClickListener: () -> Unit = {}) {
    val text = ObservableField(item.text)
}