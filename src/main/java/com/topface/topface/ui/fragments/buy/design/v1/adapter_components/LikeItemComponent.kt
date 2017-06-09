package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.ItemPurchaseLikeBinding
import com.topface.topface.ui.fragments.buy.design.v1.LikeItem
import com.topface.topface.ui.fragments.buy.design.v1.view_models.LikeItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class LikeItemComponent(private val mNavigator: IFeedNavigator) : AdapterComponent<ItemPurchaseLikeBinding, LikeItem>() {
    override val itemLayout = R.layout.item_purchase_like
    override val bindingClass = ItemPurchaseLikeBinding::class.java

    override fun bind(binding: ItemPurchaseLikeBinding, data: LikeItem?, position: Int) {
        with(binding) {
            (this.root.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.isFullSpan = true
            data?.let {
                viewModel = LikeItemViewModel(it.data, it.from, mNavigator)
            }
        }
    }
}