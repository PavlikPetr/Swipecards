package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.topface.data.FeedUser
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

class ChatTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        FeedUser::class.java -> 1
        else -> 0
    }
}