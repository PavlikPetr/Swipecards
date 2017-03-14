package com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components

import com.topface.topface.R
import com.topface.topface.databinding.BottomSheetTitleBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetTitle
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models.SettingsPaymentNinjaBottomSheetItemViewModel

/**
 * Компонент для title bottom sheet экрана платежей payment ninja
 * Created by petrp on 09.03.2017.
 */
class BottomSheetTitleComponent : AdapterComponent<BottomSheetTitleBinding, BottomSheetTitle>() {

    override val itemLayout: Int
        get() = R.layout.bottom_sheet_title
    override val bindingClass: Class<BottomSheetTitleBinding>
        get() = BottomSheetTitleBinding::class.java

    override fun bind(binding: BottomSheetTitleBinding, data: BottomSheetTitle?, position: Int) {
        data?.let {
            binding.viewModel = SettingsPaymentNinjaBottomSheetItemViewModel(it.title) {}
        }
    }

}