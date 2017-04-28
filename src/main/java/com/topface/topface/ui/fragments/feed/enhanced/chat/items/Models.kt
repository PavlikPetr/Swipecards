package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.api.responses.HistoryItem

data class UserGift(val data: HistoryItem)
data class FriendGift(val data: HistoryItem)
data class UserMessage(val data: HistoryItem)
data class FriendMessage(val data: HistoryItem)
data class Divider(val data: HistoryItem)