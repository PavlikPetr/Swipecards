package com.topface.topface.ui.fragments.buy.pn_purchase.components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenProductUnavailable
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Payment Ninja purchase screen with unavailable products component
 * Created by petrp on 03.03.2017.
 */
class BuyScreenUnavailableComponent : AdapterComponent<TextWrappedHeightMaxWidthBinding, BuyScreenProductUnavailable>() {
    override val itemLayout: Int
        get() = R.layout.text_wrapped_height_max_width
    override val bindingClass: Class<TextWrappedHeightMaxWidthBinding>
        get() = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: BuyScreenProductUnavailable?, position: Int) {
        binding.viewModel = BuyScreenTextViewModel.PaymentNinjaPurchaseUnavailable()
        binding.root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }
}