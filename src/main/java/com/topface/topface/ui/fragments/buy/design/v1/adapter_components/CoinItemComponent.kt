package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.ItemPurchaseCoinBinding
import com.topface.topface.ui.fragments.buy.design.v1.CoinItem
import com.topface.topface.ui.fragments.buy.design.v1.view_models.CoinItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class CoinItemComponent(private val mNavigator: IFeedNavigator) : AdapterComponent<ItemPurchaseCoinBinding, CoinItem>() {
    override val itemLayout = R.layout.item_purchase_coin
    override val bindingClass = ItemPurchaseCoinBinding::class.java

    override fun bind(binding: ItemPurchaseCoinBinding, data: CoinItem?, position: Int) {
        data?.let {
            binding.viewModel = CoinItemViewModel(it.data, it.from,it.img, mNavigator)
        }
    }
}