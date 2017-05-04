package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.R
import com.topface.topface.databinding.ItemChatD1UserMessageBinding
import com.topface.topface.ui.fragments.feed.enhanced.chat.UserMessage
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class UserMessageComponent : AdapterComponent<ItemChatD1UserMessageBinding, UserMessage>() {
    override val itemLayout: Int
        get() = R.layout.item_chat_d1_user_message
    override val bindingClass: Class<ItemChatD1UserMessageBinding>
        get() = ItemChatD1UserMessageBinding::class.java

    override fun bind(binding: ItemChatD1UserMessageBinding, data: UserMessage?, position: Int) {
        data?.let {
            binding.viewModel = UserMessageViewModel(it, position)
        }
    }
}