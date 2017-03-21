package com.topface.topface.ui.settings.payment_ninja

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.FragmentSettingsPaymentsBinding
import com.topface.topface.ui.dialogs.AlertDialogFactory
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetData
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType
import com.topface.topface.ui.settings.payment_ninja.components.CardComponent
import com.topface.topface.ui.settings.payment_ninja.components.HelpComponent
import com.topface.topface.ui.settings.payment_ninja.components.LoaderComponent
import com.topface.topface.ui.settings.payment_ninja.components.SubscriptionComponent
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.isAvailable
import org.jetbrains.anko.layoutInflater

/**
 * Screen for current users cards and subscriptions
 * Created by ppavlik on 06.03.17.
 */

class SettingsPaymentsNinjaFragment : BaseFragment(), IAlertDialog {

    private val mBinding by lazy {
        DataBindingUtil.inflate<FragmentSettingsPaymentsBinding>(context.layoutInflater,
                R.layout.fragment_settings_payments, null, false)
    }

    private val mViewModel by lazy {
        SettingsPaymentNinjaViewModel(mFeedNavigator, this)
    }

    private val mTypeProvider by lazy {
        SettingsPaymentNinjaTypeProvider()
    }

    private val mFeedNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mTypeProvider) { Bundle() }
                .addAdapterComponent(CardComponent(mOnClick = {
                    mViewModel.getCardInfo()?.let {
                        if (!it.isAvailable()) {
                            mFeedNavigator.showPaymentNinjaPurchaseProduct(true)
                        }
                    }
                }, mOnLongClick = {
                    mViewModel.getCardInfo()?.let {
                        if (it.isAvailable()) {
                            mFeedNavigator.showPaymentNinjaBottomSheet(ModalBottomSheetData(
                                    ModalBottomSheetType(ModalBottomSheetType.CARD_BOTTOM_SHEET), it))
                        }
                        it.isAvailable()
                    } ?: false
                }))
                .addAdapterComponent(HelpComponent(mFeedNavigator))
                .addAdapterComponent(SubscriptionComponent(mFeedNavigator))
                .addAdapterComponent(LoaderComponent())
    }

    private fun initList() = with(mBinding.list) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
        addItemDecoration(PurchasesItemDecorator())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        return mBinding.apply {
            viewModel = mViewModel
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

    override fun show(positive: () -> Unit) {
        AlertDialogFactory().constructDeleteCard(activity, positive)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mViewModel.onActivityResult(requestCode, resultCode, data)
    }
}