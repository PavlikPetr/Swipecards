package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.ItemPurchaseCoinBinding
import com.topface.topface.ui.fragments.buy.design.v1.CoinItem
import com.topface.topface.ui.fragments.buy.design.v1.CoinItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class CoinItemComponent: AdapterComponent<ItemPurchaseCoinBinding, CoinItem>() {
    override val itemLayout = R.layout.item_purchase_coin
    override val bindingClass = ItemPurchaseCoinBinding::class.java

    override fun bind(binding: ItemPurchaseCoinBinding, data: CoinItem?, position: Int) {
        with(binding) {
            viewModel = CoinItemViewModel()
        }
    }
}