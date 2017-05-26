package com.topface.topface.ui.fragments.buy.pn_purchase.components

import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.BlackListItem
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenCoinsSection
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Blacl list item
 * Created by petrp on 26.05.2017.
 */
class BlackListItemComponent(val mFeedNavigator: IFeedNavigator) : AdapterComponent<TextWrappedHeightMaxWidthBinding, BlackListItem>() {
    override val itemLayout: Int
        get() = R.layout.text_wrapped_height_max_width
    override val bindingClass: Class<TextWrappedHeightMaxWidthBinding>
        get() = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: BlackListItem?, position: Int) {
        binding.viewModel = BuyScreenTextViewModel.PaymentNinjaBlackListItem { mFeedNavigator.showBlackList() }
    }
}