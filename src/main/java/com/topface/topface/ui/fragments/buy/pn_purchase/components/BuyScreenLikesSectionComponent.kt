package com.topface.topface.ui.fragments.buy.pn_purchase.components

import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenLikesSection
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Payment Ninja purchase screen title component
 * Created by petrp on 03.03.2017.
 */
class BuyScreenLikesSectionComponent : AdapterComponent<TextWrappedHeightMaxWidthBinding, BuyScreenLikesSection>() {
    override val itemLayout: Int
        get() = R.layout.text_wrapped_height_max_width
    override val bindingClass: Class<TextWrappedHeightMaxWidthBinding>
        get() = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: BuyScreenLikesSection?, position: Int) {
        binding.viewModel = BuyScreenTextViewModel.PaymentNinjaLikesSection()
    }
}