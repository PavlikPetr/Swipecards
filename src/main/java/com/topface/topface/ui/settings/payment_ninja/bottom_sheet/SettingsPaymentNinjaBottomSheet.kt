package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.bottom_sheet.BottomSheetBase
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components.BottomSheetItemComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components.BottomSheetTitleComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models.SettingsPaymentNinjaBottomSheetViewModel
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getString

/**
 * Конфигурируем bottom sheet для экрана покупок payment ninja
 * Created by petrp on 09.03.2017.
 */
class SettingsPaymentNinjaBottomSheet(view: RecyclerView) : BottomSheetBase<RecyclerView>(view), ISettingsPaymentNinjaBottomSheetInterface {
    private val mTypeProvider by lazy {
        SettingsPaymentNinjaBottomSheetTypeProvider()
    }
    private val mAdapter by lazy {
        CompositeAdapter(mTypeProvider) { Bundle() }
                .addAdapterComponent(BottomSheetTitleComponent())
                .addAdapterComponent(BottomSheetItemComponent())
    }

    override fun configurateBottomSheet(bottoSheet: BottomSheetBehavior<RecyclerView>) {
        bottoSheet.state = BottomSheetBehavior.STATE_HIDDEN
        bottoSheet.isHideable = false
    }

    val viewModel by lazy {
        SettingsPaymentNinjaBottomSheetViewModel { show() }
    }

    override fun showCardBottomSheet() {
        viewModel.showCardBottomSheet()
    }

    override fun showSubscriptionBottomSheet(isSubscriptionActive: Boolean) {
        viewModel.showSubscriptionBottomSheet(isSubscriptionActive)
    }

    init {
        with(view) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }
}
