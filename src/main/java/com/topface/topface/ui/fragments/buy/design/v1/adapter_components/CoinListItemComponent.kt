package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.ItemPurchaseCoinListBinding
import com.topface.topface.ui.fragments.buy.design.v1.CoinListItem
import com.topface.topface.ui.fragments.buy.design.v1.CoinListItemViewModel
import com.topface.topface.ui.fragments.buy.design.v1.PurchaseItemDecoration
import com.topface.topface.ui.fragments.buy.design.v1.TypeProvider
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.getInt

class CoinListItemComponent: AdapterComponent<ItemPurchaseCoinListBinding, CoinListItem>() {
    override val itemLayout = R.layout.item_purchase_coin_list
    override val bindingClass = ItemPurchaseCoinListBinding::class.java

    override fun bind(binding: ItemPurchaseCoinListBinding, data: CoinListItem?, position: Int) {
        with(binding) {
            coinList.layoutManager = StaggeredGridLayoutManager(R.integer.purchase_v1_coins_on_row.getInt(),
                    StaggeredGridLayoutManager.VERTICAL)
            coinList.adapter = CompositeAdapter(TypeProvider()) { Bundle() }
                    .addAdapterComponent(CoinItemComponent())
            viewModel = CoinListItemViewModel()
            coinList.addItemDecoration(PurchaseItemDecoration())
        }
    }
}