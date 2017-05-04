package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.R
import com.topface.topface.databinding.ItemChatD1UserGiftBinding
import com.topface.topface.ui.fragments.feed.enhanced.chat.UserGift
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class UserGiftComponent(val feedNavigator: FeedNavigator): AdapterComponent<ItemChatD1UserGiftBinding, UserGift>() {
    override val itemLayout: Int
        get() = R.layout.item_chat_d1_user_gift
    override val bindingClass: Class<ItemChatD1UserGiftBinding>
        get() = ItemChatD1UserGiftBinding::class.java

    override fun bind(binding: ItemChatD1UserGiftBinding, data: UserGift?, position: Int) {
        data?.let {
            binding.viewModel = GiftViewModel(it, longClickListener = {
                feedNavigator.showChatPopupMenu(it, position)
                true
            })
        }
    }
}