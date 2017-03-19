package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.PaymentsBottomSheetBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components.BottomSheetItemComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.components.BottomSheetTitleComponent
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models.SettingsPaymentNinjaModalBottomSheetViewModel
import com.topface.topface.utils.IActivityDelegate
import org.jetbrains.anko.layoutInflater

/**
 * Модальный bottom sheet
 * Created by ppavlik on 15.03.17.
 */
class SettingsPaymentNinjaModalBottomSheet : BottomSheetDialogFragment() {
    companion object {
        private const val DATA = "bottom_sheet_data"
        const val TAG = "settings_payment_ninja_modal_bottom_sheet_tag"
        fun newInstance(data: ModalBottomSheetData) =
                SettingsPaymentNinjaModalBottomSheet().apply {
                    arguments = Bundle().apply {
                        putParcelable(DATA, data)
                    }
                }
    }

    private lateinit var mData: ModalBottomSheetData

    private val mViewModel by lazy {
        SettingsPaymentNinjaModalBottomSheetViewModel(mData)
    }

    private val mTypeProvider by lazy {
        SettingsPaymentNinjaBottomSheetTypeProvider()
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<PaymentsBottomSheetBinding>(context.layoutInflater,
                R.layout.payments_bottom_sheet, null, false)
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mTypeProvider) { Bundle() }
                .addAdapterComponent(BottomSheetTitleComponent())
                .addAdapterComponent(BottomSheetItemComponent { dismiss()})
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mData = arguments.getParcelable(DATA)
    }

    private fun initList() = with(mBinding.bottomSheetList) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        return mBinding.apply { viewModel = mViewModel }.root
    }
}