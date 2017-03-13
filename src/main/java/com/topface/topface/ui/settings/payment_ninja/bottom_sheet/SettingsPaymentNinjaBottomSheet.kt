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
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.getString

/**
 * Конфигурируем bottom sheet для экрана покупок payment ninja
 * Created by petrp on 09.03.2017.
 */
class SettingsPaymentNinjaBottomSheet(private val mView: RecyclerView) : BottomSheetBase<RecyclerView>(), ISettingsPaymentNinjaBottomSheetInterface {
    private val mTypeProvider by lazy {
        SettingsPaymentNinjaBottomSheetTypeProvider()
    }
    private val mAdapter by lazy {
        CompositeAdapter(mTypeProvider) { Bundle() }
                .addAdapterComponent(BottomSheetTitleComponent())
                .addAdapterComponent(BottomSheetItemComponent())
    }

    override val mBottomSheetLayout: RecyclerView
        get() = mView

    override fun configurateBottomSheet(bottoSheet: BottomSheetBehavior<RecyclerView>) {
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
        with(mView) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }
}
