package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import java.util.*

/**
 * Модальный bottom sheet
 * Created by ppavlik on 15.03.17.
 */
class SettingsPaymentNinjaModalBottomSheet : BottomSheetDialogFragment() {
    companion object {
        private const val LIST = "items_list"
        fun newInstance(list: ArrayList<Any>) =
                SettingsPaymentNinjaModalBottomSheet().apply {
                    arguments = Bundle().apply {
                    }
                }

    }
}