package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import com.topface.topface.R
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getString

class SettingsPaymentNinjaBottomSheetViewModel(private val mShowCallback: () -> Unit) : ISettingsPaymentNinjaBottomSheetInterface {
    val data = SingleObservableArrayList<Any>()
    override fun showCardBottomSheet() {
        with(data.observableList) {
            clear()
            //TODO дернуть extension
            add(BottomSheetTitle(""))
            add(BOTTOM_SHEET_ITEMS_POOL.USE_ANOTHER_CARD.textRes.getString())
            add(BOTTOM_SHEET_ITEMS_POOL.DELETE_CARD.textRes.getString())
        }
        mShowCallback.invoke()
    }

    override fun showSubscriptionBottomSheet(isSubscriptionActive: Boolean) {
        with(data.observableList) {
            clear()
            add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
            add(if (isSubscriptionActive)
                BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION.textRes.getString()
            else
                BOTTOM_SHEET_ITEMS_POOL.RESUME_SUBSCRIPTION.textRes.getString())
        }
        mShowCallback.invoke()
    }
}