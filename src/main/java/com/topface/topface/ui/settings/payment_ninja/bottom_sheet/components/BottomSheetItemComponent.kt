package com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components

import com.topface.topface.R
import com.topface.topface.databinding.BottomSheetTitleBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BOTTOM_SHEET_ITEMS_POOL
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models.SettingsPaymentNinjaBottomSheetItemViewModel
import com.topface.topface.utils.extensions.getString

/**
 * Компонент для title bottom sheet экрана платежей payment ninja
 * Created by petrp on 09.03.2017.
 */
class BottomSheetItemComponent : AdapterComponent<BottomSheetTitleBinding, BOTTOM_SHEET_ITEMS_POOL>() {

    override val itemLayout: Int
        get() = R.layout.bottom_sheet_title
    override val bindingClass: Class<BottomSheetTitleBinding>
        get() = BottomSheetTitleBinding::class.java

    override fun bind(binding: BottomSheetTitleBinding, data: BOTTOM_SHEET_ITEMS_POOL?, position: Int) {
        data?.let { item ->
            binding.viewModel = SettingsPaymentNinjaBottomSheetItemViewModel(item.textRes.getString()) {
                when (item) {
                    BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION -> TODO()
                    BOTTOM_SHEET_ITEMS_POOL.RESUME_SUBSCRIPTION -> TODO()
                    BOTTOM_SHEET_ITEMS_POOL.DELETE_CARD -> TODO()
                    BOTTOM_SHEET_ITEMS_POOL.USE_ANOTHER_CARD -> TODO()
                }
            }
        }
    }

}