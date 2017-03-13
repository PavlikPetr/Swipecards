package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.bottom_sheet.BottomSheetBase
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components.BottomSheetItemComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components.BottomSheetTitleComponent
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getString

/**
 * Конфигурируем bottom sheet для экрана покупок payment ninja
 * Created by petrp on 09.03.2017.
 */
class SettingsPaymentNinjaBottomSheet<V : View>(private val mView: V) : BottomSheetBase<V>(), ISettingsPaymentNinjaBottomSheetInterface {
    private val mTypeProvider by lazy {
        SettingsPaymentNinjaBottomSheetTypeProvider()
    }
    val mCompositedapter by lazy {
        CompositeAdapter(mTypeProvider) { Bundle() }
    }

    override val mBottomSheetLayout: V
        get() = mView

    private val mData = SingleObservableArrayList<Any>()

    override fun configurateBottomSheet(bottoSheet: BottomSheetBehavior<V>) {
    }

    init {
        mCompositedapter
                .addAdapterComponent(BottomSheetTitleComponent())
                .addAdapterComponent(BottomSheetItemComponent())
    }

    override fun showCardBottomSheet() {
        with(mData.observableList) {
            clear()
            //TODO дернуть extension
            add(BottomSheetTitle(""))
            add(BOTTOM_SHEET_ITEMS_POOL.USE_ANOTHER_CARD.textRes.getString())
            add(BOTTOM_SHEET_ITEMS_POOL.DELETE_CARD.textRes.getString())
        }
        show()
    }

    override fun showSubscriptionBottomSheet(isSubscriptionActive: Boolean) {
        with(mData.observableList) {
            clear()
            add(BottomSheetTitle(R.string.ninja_vip_status_title.getString()))
            add(if (isSubscriptionActive)
                BOTTOM_SHEET_ITEMS_POOL.CANCEL_SUBSCRIPTION.textRes.getString()
            else
                BOTTOM_SHEET_ITEMS_POOL.RESUME_SUBSCRIPTION.textRes.getString())
        }
        show()
    }
}
