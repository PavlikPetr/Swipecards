package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.R
import com.topface.topface.databinding.ItemChatD1FriendMessageBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class FriendMessageCompnent: AdapterComponent<ItemChatD1FriendMessageBinding, FriendMessage>() {
    override val itemLayout: Int
        get() = R.layout.item_chat_d1_friend_message
    override val bindingClass: Class<ItemChatD1FriendMessageBinding>
        get() = ItemChatD1FriendMessageBinding::class.java

    override fun bind(binding: ItemChatD1FriendMessageBinding, data: FriendMessage?, position: Int) {
        data?.let { binding.viewModel = FriendMessageViewModel(it) }
    }
}