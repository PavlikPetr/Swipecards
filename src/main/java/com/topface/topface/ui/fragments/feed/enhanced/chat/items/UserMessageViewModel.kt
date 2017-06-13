package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.App
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.ui.fragments.feed.enhanced.chat.SendHistoryItemEvent

class UserMessageViewModel(item: HistoryItem, itemPosition: Int) : BaseMessageViewModel(item, itemPosition), IResendableItem {
    override val isSending = item.isSending
    override val isRetrierVisible = item.isRetrierVisible
    override val isErrorVisible = item.isErrorVisible

    override fun onRetryClick() {
        if (!isSending.get()) {
            isSending.set(true)
            App.getAppComponent().eventBus().setData(SendHistoryItemEvent(item))
        }
    }
}