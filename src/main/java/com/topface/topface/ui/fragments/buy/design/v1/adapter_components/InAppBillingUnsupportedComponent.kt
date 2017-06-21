package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.design.v1.InAppBillingUnsupported
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class InAppBillingUnsupportedComponent : AdapterComponent<TextWrappedHeightMaxWidthBinding, InAppBillingUnsupported>() {
    override val itemLayout = R.layout.text_wrapped_height_max_width
    override val bindingClass = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: InAppBillingUnsupported?, position: Int) {
        with(binding) {
            root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                    StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
            viewModel = BuyScreenTextViewModel.InAppBillingUnsupportedItem()
        }
    }
}