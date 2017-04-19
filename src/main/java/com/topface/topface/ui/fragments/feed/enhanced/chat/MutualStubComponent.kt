package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.MutualStubChatBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Заглушка "взаимных симпатий" в чате
 */
class MutualStubComponent(private val mMutualItem: FeedUser = FeedUser()) : AdapterComponent<MutualStubChatBinding, FeedUser>() {

    private val mViewModel by lazy {
        MutualStubChatViewModel(mMutualItem)
    }
    override val itemLayout: Int
        get() = R.layout.mutual_stub_chat
    override val bindingClass: Class<MutualStubChatBinding>
        get() = MutualStubChatBinding::class.java

    override fun bind(binding: MutualStubChatBinding, data: FeedUser?, position: Int) =
            with(binding) {
                setViewModel(mViewModel)
                root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
            }
}