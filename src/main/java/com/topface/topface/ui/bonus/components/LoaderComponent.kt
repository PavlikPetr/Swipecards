package com.topface.topface.ui.bonus.components

import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager.*
import com.topface.topface.R
import com.topface.topface.databinding.ItemLoaderBinding
import com.topface.topface.ui.bonus.Loader
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент лоадера
 * Created by petrp on 02.06.2017.
 */

class LoaderComponent : AdapterComponent<ItemLoaderBinding, Loader>() {
    override val itemLayout = R.layout.item_loader
    override val bindingClass = ItemLoaderBinding::class.java

    override fun bind(binding: ItemLoaderBinding, data: Loader?, position: Int) {
        binding.root.layoutParams = StaggeredGridLayoutManager.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }
}