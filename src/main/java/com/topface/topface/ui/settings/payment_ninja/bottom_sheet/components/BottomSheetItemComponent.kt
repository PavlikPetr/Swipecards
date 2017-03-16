package com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.TextWrappedHeightMaxWidthBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.BuyScreenTextViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BOTTOM_SHEET_ITEMS_POOL
import com.topface.topface.utils.extensions.getString

/**
 * Компонент для title bottom sheet экрана платежей payment ninja
 * Created by petrp on 09.03.2017.
 */
class BottomSheetItemComponent(private val mCloseListener: () -> Unit) : AdapterComponent<TextWrappedHeightMaxWidthBinding, BOTTOM_SHEET_ITEMS_POOL>() {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    override val itemLayout: Int
        get() = R.layout.text_wrapped_height_max_width
    override val bindingClass: Class<TextWrappedHeightMaxWidthBinding>
        get() = TextWrappedHeightMaxWidthBinding::class.java

    override fun bind(binding: TextWrappedHeightMaxWidthBinding, data: BOTTOM_SHEET_ITEMS_POOL?, position: Int) {
        data?.let { item ->
            binding.viewModel = BuyScreenTextViewModel.PaymentNinjaBottomSheetItem(item.textRes.getString()) {
                mEventBus.setData(item)
                mCloseListener.invoke()
            }
        }
    }
}