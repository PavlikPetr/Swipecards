package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.R
import com.topface.topface.databinding.ItemChatD1DividerBinding
import com.topface.topface.ui.fragments.feed.enhanced.chat.Divider
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class DividerComponent: AdapterComponent<ItemChatD1DividerBinding, Divider>() {
    override val itemLayout: Int
        get() = R.layout.item_chat_d1_divider
    override val bindingClass: Class<ItemChatD1DividerBinding>
        get() = ItemChatD1DividerBinding::class.java

    override fun bind(binding: ItemChatD1DividerBinding, data: Divider?, position: Int) {
        data?.let { binding.viewModel = DividerViewModel(it) }
    }
}