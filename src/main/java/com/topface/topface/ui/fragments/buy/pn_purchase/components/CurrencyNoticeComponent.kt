package com.topface.topface.ui.fragments.buy.pn_purchase.components

import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenLikesSection
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.fragments.buy.pn_purchase.CurrencyNotice
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент с инфой о том, что размер списания средств может отличаться от стоимости указанной на кнопке продукта
 * Created by petrp on 26.06.2017.
 */
class CurrencyNoticeComponent : AdapterComponent<TextWrappedHeightMaxWidthBinding, CurrencyNotice>() {
    override val itemLayout: Int
        get() = R.layout.text_wrapped_height_max_width
    override val bindingClass: Class<TextWrappedHeightMaxWidthBinding>
        get() = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: CurrencyNotice?, position: Int) {
        binding.viewModel = BuyScreenTextViewModel.CurrencyNotice()
    }
}