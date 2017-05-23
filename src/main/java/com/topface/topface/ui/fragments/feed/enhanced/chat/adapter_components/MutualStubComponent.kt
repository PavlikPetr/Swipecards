package com.topface.topface.ui.fragments.feed.enhanced.chat.adapter_components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.databinding.MutualStubChatBinding
import com.topface.topface.ui.fragments.feed.enhanced.chat.MutualStub
import com.topface.topface.ui.fragments.feed.enhanced.chat.stubs.MutualStubChatViewModel

/**
 * Заглушка "взаимных симпатий" в чате
 */
class MutualStubComponent(private val mFeedUser: com.topface.topface.data.FeedUser?) : com.topface.topface.ui.new_adapter.enhanced.AdapterComponent<MutualStubChatBinding, MutualStub>() {

    override val itemLayout: Int
        get() = com.topface.topface.R.layout.mutual_stub_chat
    override val bindingClass: Class<com.topface.topface.databinding.MutualStubChatBinding>
        get() = com.topface.topface.databinding.MutualStubChatBinding::class.java

    private var mViewModel: MutualStubChatViewModel? = null

    override fun bind(binding: com.topface.topface.databinding.MutualStubChatBinding, data: com.topface.topface.ui.fragments.feed.enhanced.chat.MutualStub?, position: Int) =
            with(binding) {
                mFeedUser?.let { mViewModel = MutualStubChatViewModel(it) }
                viewModel = mViewModel
                root.layoutParams = android.support.v7.widget.StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
            }

    override fun release() {
        super.release()
        mViewModel?.release()
    }
}