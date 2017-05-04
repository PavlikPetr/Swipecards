package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.R
import com.topface.topface.databinding.ItemChatD1FriendGiftBinding
import com.topface.topface.ui.fragments.feed.enhanced.chat.FriendGift
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class FriendGiftComponent(val feedNavigator: FeedNavigator): AdapterComponent<ItemChatD1FriendGiftBinding, FriendGift>() {
    override val itemLayout: Int
        get() = R.layout.item_chat_d1_friend_gift
    override val bindingClass: Class<ItemChatD1FriendGiftBinding>
        get() = ItemChatD1FriendGiftBinding::class.java

    override fun bind(binding: ItemChatD1FriendGiftBinding, data: FriendGift?, position: Int) {
        data?.let {
            binding.viewModel = GiftViewModel(it, longClickListener = {
                feedNavigator.showChatPopupMenu(it, position)
                true
            })
        }
    }
}