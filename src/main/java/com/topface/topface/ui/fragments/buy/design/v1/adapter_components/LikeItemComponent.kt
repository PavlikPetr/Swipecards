package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.ItemPurchaseLikeBinding
import com.topface.topface.ui.fragments.buy.design.v1.LikeItem
import com.topface.topface.ui.fragments.buy.design.v1.LikeItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class LikeItemComponent: AdapterComponent<ItemPurchaseLikeBinding, LikeItem>() {
    override val itemLayout = R.layout.item_purchase_like
    override val bindingClass = ItemPurchaseLikeBinding::class.java

    override fun bind(binding: ItemPurchaseLikeBinding, data: LikeItem?, position: Int) {
        with(binding) {
            viewModel = LikeItemViewModel()
        }
    }
}