package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Payment ninja bottom sheet type provider
 * Created by petrp on 09.03.2017.
 */
class SettingsPaymentNinjaBottomSheetTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        BottomSheetTitle::class.java -> 1
        BottomSheetData::class.java -> 2
        else -> 0
    }
}