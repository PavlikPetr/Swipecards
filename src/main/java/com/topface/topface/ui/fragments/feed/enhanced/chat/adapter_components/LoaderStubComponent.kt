package com.topface.topface.ui.fragments.feed.enhanced.chat.adapter_components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.ItemLoaderBinding
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatLoader
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для итема с лоадером
 */
class LoaderStubComponent : AdapterComponent<ItemLoaderBinding, ChatLoader>() {
    override val itemLayout: Int
        get() = R.layout.item_loader
    override val bindingClass: Class<ItemLoaderBinding>
        get() = ItemLoaderBinding::class.java

    override fun bind(binding: ItemLoaderBinding, data: ChatLoader?, position: Int) {
        binding.root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }
}