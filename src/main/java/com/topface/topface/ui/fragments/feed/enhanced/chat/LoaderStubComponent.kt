package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.LoaderStubChatBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для итема с лоадером
 */
class LoaderStubComponent : AdapterComponent<LoaderStubChatBinding, ChatLoader>() {
    override val itemLayout: Int
        get() = R.layout.loader_stub_chat
    override val bindingClass: Class<LoaderStubChatBinding>
        get() = LoaderStubChatBinding::class.java

    override fun bind(binding: LoaderStubChatBinding, data: ChatLoader?, position: Int) {
        binding.root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }
}