package com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components

import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetTitle

/**
 * Компонент для title bottom sheet экрана платежей payment ninja
 * Created by petrp on 09.03.2017.
 */
class BottomSheetTitleComponent : AdapterComponent<TextWrappedHeightMaxWidthBinding, BottomSheetTitle>() {

    override val itemLayout: Int
        get() = R.layout.text_wrapped_height_max_width
    override val bindingClass: Class<TextWrappedHeightMaxWidthBinding>
        get() = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: BottomSheetTitle?, position: Int) {
        data?.let {
            binding.viewModel = BuyScreenTextViewModel.PaymentNinjaBottomSheetTitle(it.title)
        }
    }

}