package com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BOTTOM_SHEET_ITEMS_POOL
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetTitle
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CANCEL_AUTOFILLING_BOTTOM_SHEET
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CANCEL_SUBSCRIPTION_BOTTOM_SHEET
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CARD_BOTTOM_SHEET
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.RESTORE_SUBSCRIPTION_BOTTOM_SHEET
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getCardName
import com.topface.topface.utils.extensions.getString

/**
 * Вью-модель диалога моального bottomSheet
 * Created by ppavlik on 16.03.17.
 */
class SettingsPaymentNinjaModalBottomSheetViewModel(bottomSheetType: ModalBottomSheetType) {
    val data = SingleObservableArrayList<Any>()

    init {
        when (bottomSheetType.type) {
            CARD_BOTTOM_SHEET -> {
                with(data.observableList) {
                    clear()
                    val info = App.get().options.paymentNinjaInfo
                    add(BottomSheetTitle(CardInfo(info.lastDigit, info.type).getCardName()))
                    add(BOTTOM_SHEET_ITEMS_POOL.USE_ANOTHER_CARD)
                    add(BOTTOM_SHEET_ITEMS_POOL.DELETE_CARD)
                }
            }
            RESTORE_SUBSCRIPTION_BOTTOM_SHEET -> {
                with(data.observableList) {
                    clear()
                    add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
                    add(BOTTOM_SHEET_ITEMS_POOL.RESUME_SUBSCRIPTION)
                }
            }
            CANCEL_SUBSCRIPTION_BOTTOM_SHEET -> {
                with(data.observableList) {
                    clear()
                    add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
                    add(BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION)
                }
            }
            CANCEL_AUTOFILLING_BOTTOM_SHEET -> {
                with(data.observableList) {
                    clear()
                    add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
                    add(BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION)
                }
            }
        }
    }
}