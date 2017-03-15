package com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BOTTOM_SHEET_ITEMS_POOL
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetTitle
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ISettingsPaymentNinjaBottomSheetInterface
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getCardName
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString

class SettingsPaymentNinjaBottomSheetViewModel(private val mShowCallback: () -> Unit,
                                               private val mSetPeekHeight: (peekHeight: Int) -> Unit) : ISettingsPaymentNinjaBottomSheetInterface {
    val data = SingleObservableArrayList<Any>()
    override fun showCardBottomSheet() {
        with(data.observableList) {
            clear()
            val info = App.get().options.paymentNinjaInfo
            add(BottomSheetTitle(CardInfo(info.lastDigit, info.type).getCardName()))
            add(BOTTOM_SHEET_ITEMS_POOL.USE_ANOTHER_CARD)
            add(BOTTOM_SHEET_ITEMS_POOL.DELETE_CARD)
        }
        setPeekHeight()
        mShowCallback.invoke()
    }

    override fun showSubscriptionBottomSheet(isSubscriptionActive: Boolean) {
        with(data.observableList) {
            clear()
            add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
            add(if (isSubscriptionActive)
                BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION
            else
                BOTTOM_SHEET_ITEMS_POOL.RESUME_SUBSCRIPTION)
        }
        setPeekHeight()
        mShowCallback.invoke()
    }

    private fun setPeekHeight() {
        mSetPeekHeight.invoke((data.observableList.filter { it is BottomSheetTitle }.size * R.dimen.payment_ninja_payments_items_height.getDimen() +
                data.observableList.filter { it is BOTTOM_SHEET_ITEMS_POOL }.size * R.dimen.payment_ninja_payments_items_height.getDimen()).toInt())
    }
}