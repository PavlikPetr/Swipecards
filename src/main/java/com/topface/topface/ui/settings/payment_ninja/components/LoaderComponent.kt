package com.topface.topface.ui.settings.payment_ninja.components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.ItemLoaderBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.PaymnetNinjaPurchasesLoader

/**
 * Компонент лоадера
 * Created by petrp on 09.03.2017.
 */

class LoaderComponent : AdapterComponent<ItemLoaderBinding, PaymnetNinjaPurchasesLoader>() {
    override val itemLayout: Int
        get() = R.layout.item_loader
    override val bindingClass: Class<ItemLoaderBinding>
        get() = ItemLoaderBinding::class.java

    override fun bind(binding: ItemLoaderBinding, data: PaymnetNinjaPurchasesLoader?, position: Int) {
        binding.root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }
}