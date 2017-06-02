package com.topface.topface.ui.bonus.components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.databinding.ItemLoaderBinding
import com.topface.topface.ui.bonus.Loader

/**
 * Компонент лоадера
 * Created by petrp on 02.06.2017.
 */

class LoaderComponent : com.topface.topface.ui.new_adapter.enhanced.AdapterComponent<ItemLoaderBinding, Loader>() {
    override val itemLayout: Int
        get() = com.topface.topface.R.layout.item_loader
    override val bindingClass: Class<com.topface.topface.databinding.ItemLoaderBinding>
        get() = com.topface.topface.databinding.ItemLoaderBinding::class.java

    override fun bind(binding: com.topface.topface.databinding.ItemLoaderBinding, data: com.topface.topface.ui.bonus.Loader?, position: Int) {
        binding.root.layoutParams = android.support.v7.widget.StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }
}