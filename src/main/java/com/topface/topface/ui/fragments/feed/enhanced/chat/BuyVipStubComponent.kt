package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.BuyVipStubChatBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент заглушки "купите Vip" в редизайне чата
 */
class BuyVipStubComponent(private val mFeed: FeedUser, private val mFeedNavigator: FeedNavigator) : AdapterComponent<BuyVipStubChatBinding, FeedUser>() {

    private val mViewModel by lazy {
        BuyVipStubViewModel(mFeed, mFeedNavigator)
    }

    override val itemLayout: Int
        get() = R.layout.buy_vip_stub_chat
    override val bindingClass: Class<BuyVipStubChatBinding>
        get() = BuyVipStubChatBinding::class.java

    override fun bind(binding: BuyVipStubChatBinding, data: FeedUser?, position: Int) =
            with(binding) {
                setViewModel(mViewModel)
                root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
            }
}