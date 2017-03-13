package com.topface.topface.ui.settings.payment_ninja

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.FragmentSettingsPaymentsBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.settings.payment_ninja.components.LoaderComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.SettingsPaymentNinjaBottomSheet
import com.topface.topface.ui.settings.payment_ninja.components.CardComponent
import com.topface.topface.ui.settings.payment_ninja.components.HelpComponent
import com.topface.topface.ui.settings.payment_ninja.components.SubscriptionComponent
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.extensions.getString
import org.jetbrains.anko.layoutInflater

/**
 * Screen for current users cards and subscriptions
 * Created by ppavlik on 06.03.17.
 */

class SettingsPaymentsNinjaFragment : BaseFragment() {

    private val mBinding by lazy {
        DataBindingUtil.inflate<FragmentSettingsPaymentsBinding>(context.layoutInflater,
                R.layout.fragment_settings_payments, null, false)
    }

    private val mViewModel by lazy {
        SettingsPaymentNinjaViewModel()
    }

    private val mTypeProvider by lazy {
        SettingsPaymentNinjaTypeProvider()
    }

    private val mFeedNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mTypeProvider) { Bundle() }
                .addAdapterComponent(CardComponent(mBottomSheet))
                .addAdapterComponent(HelpComponent(mFeedNavigator))
                .addAdapterComponent(SubscriptionComponent(mBottomSheet))
                .addAdapterComponent(LoaderComponent())
    }

    private val mBottomSheet by lazy {
        SettingsPaymentNinjaBottomSheet(mBinding.bottomSheet.bottomSheetList)
    }

    private fun initList() = with(mBinding.paymentsContent.list) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
        addItemDecoration(PurchasesItemDecorator())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        return mBinding.apply {
            viewModel = mViewModel
            bottomSheetViewMDel = mBottomSheet.viewModel
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.release()
    }

    override fun onResume() {
        super.onResume()
        ToolbarManager.setToolbarSettings(ToolbarSettingsData(R.string.ninja_settings_toolbar.getString()))
    }
}