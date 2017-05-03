package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableField
import com.topface.topface.api.responses.HistoryItem

class GiftViewModel(val data: HistoryItem) {
    val link = ObservableField(data.link)
}