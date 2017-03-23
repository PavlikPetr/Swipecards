package com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetData
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetItemText
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetItemText.Companion.CANCEL_SUBSCRIPTION
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetItemText.Companion.DELETE_CARD
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetItemText.Companion.USE_ANOTHER_CARD
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetTitle
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetData
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CANCEL_AUTOFILLING_BOTTOM_SHEET
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CANCEL_SUBSCRIPTION_BOTTOM_SHEET
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CARD_BOTTOM_SHEET
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getCardName
import com.topface.topface.utils.extensions.getString

/**
 * Вью-модель диалога моального bottomSheet
 * Created by ppavlik on 16.03.17.
 */
class SettingsPaymentNinjaModalBottomSheetViewModel(bottomSheetData: ModalBottomSheetData) {
    val data = SingleObservableArrayList<Any>()

    init {
        when (bottomSheetData.type.type) {
            CARD_BOTTOM_SHEET -> {
                with(data.observableList) {
                    clear()
                    val info = App.get().options.paymentNinjaInfo
                    add(BottomSheetTitle(CardInfo(info.lastDigits, info.type).getCardName()))
                    add(BottomSheetData(BottomSheetItemText(USE_ANOTHER_CARD), bottomSheetData.data))
                    add(BottomSheetData(BottomSheetItemText(DELETE_CARD), bottomSheetData.data))
                }
            }
            CANCEL_SUBSCRIPTION_BOTTOM_SHEET -> {
                with(data.observableList) {
                    clear()
                    add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
                    add(BottomSheetData(BottomSheetItemText(CANCEL_SUBSCRIPTION), bottomSheetData.data))
                }
            }
            CANCEL_AUTOFILLING_BOTTOM_SHEET -> {
                with(data.observableList) {
                    clear()
                    add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
                    add(BottomSheetData(BottomSheetItemText(CANCEL_SUBSCRIPTION), bottomSheetData.data))
                }
            }
        }
    }
}