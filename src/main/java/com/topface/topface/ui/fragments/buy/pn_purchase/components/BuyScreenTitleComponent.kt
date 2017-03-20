package com.topface.topface.ui.fragments.buy.pn_purchase.components

import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTitle
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.Utils

/**
 * Payment Ninja purchase screen title component
 * Created by petrp on 03.03.2017.
 */
class BuyScreenTitleComponent(private val text: String?) : AdapterComponent<TextWrappedHeightMaxWidthBinding, BuyScreenTitle>() {
    override val itemLayout: Int
        get() = R.layout.text_wrapped_height_max_width
    override val bindingClass: Class<TextWrappedHeightMaxWidthBinding>
        get() = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: BuyScreenTitle?, position: Int) {
        binding.viewModel = BuyScreenTextViewModel.PaymentNinjaTitle(text ?: Utils.EMPTY, if (text.isNullOrEmpty()) View.GONE else View.VISIBLE)
    }
}