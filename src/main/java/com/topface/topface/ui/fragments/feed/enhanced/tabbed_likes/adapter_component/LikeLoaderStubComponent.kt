package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.adapter_component

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.ItemLoaderBinding
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.LoaderStub
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент с лоадера для восхищений и взаимных
 */
class LikeLoaderStubComponent : AdapterComponent<ItemLoaderBinding, LoaderStub>() {

    override val itemLayout = R.layout.item_loader
    override val bindingClass = ItemLoaderBinding::class.java

    override fun bind(binding: ItemLoaderBinding, data: LoaderStub?, position: Int) {
        binding.plc = data?.plc
        binding.root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }
}