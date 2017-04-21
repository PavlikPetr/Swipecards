package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

class ChatTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        ChatLoader::class.java -> 2
        else -> 0
    }
}