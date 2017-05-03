package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.MutualStubChatBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Заглушка "взаимных симпатий" в чате
 */
class MutualStubComponent : AdapterComponent<MutualStubChatBinding, MutualStub>() {

    override val itemLayout: Int
        get() = R.layout.mutual_stub_chat
    override val bindingClass: Class<MutualStubChatBinding>
        get() = MutualStubChatBinding::class.java

    private var mViewModel: MutualStubChatViewModel? = null

    override fun bind(binding: MutualStubChatBinding, data: MutualStub?, position: Int) =
            with(binding) {
                data?.let { mViewModel = MutualStubChatViewModel(it) }
                viewModel = mViewModel
                root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
            }

    override fun release() {
        super.release()
        mViewModel?.release()
    }
}