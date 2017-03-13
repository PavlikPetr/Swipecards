package com.topface.topface.ui.settings.payment_ninja.components

import com.topface.topface.R
import com.topface.topface.databinding.PaymentNinjaPurchaseItemTitleOnlyBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ISettingsPaymentNinjaBottomSheetInterface
import com.topface.topface.ui.settings.payment_ninja.view_models.CardViewModel

/**
 * Компонент для title bottom sheet экрана платежей payment ninja
 * Created by petrp on 09.03.2017.
 */
class CardComponent(private val mBottomSheetInterface: ISettingsPaymentNinjaBottomSheetInterface) : AdapterComponent<PaymentNinjaPurchaseItemTitleOnlyBinding, CardInfo>() {

    override val itemLayout: Int
        get() = R.layout.payment_ninja_purchase_item_title_only
    override val bindingClass: Class<PaymentNinjaPurchaseItemTitleOnlyBinding>
        get() = PaymentNinjaPurchaseItemTitleOnlyBinding::class.java

    override fun bind(binding: PaymentNinjaPurchaseItemTitleOnlyBinding, data: CardInfo?, position: Int) {
        data?.let {
            binding.viewModel = CardViewModel(it) { mBottomSheetInterface.showCardBottomSheet() }.getViewModel()
        }
    }

}