package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.billing.InstantPurchaseModel
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.banners.BannersController
import com.topface.topface.banners.IBannerAds
import com.topface.topface.databinding.DialogsFragmentLayoutBinding
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.dialogs.new_rate.RateAppFragment
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogsFragment.Companion.PAGE_NAME
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.*
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.findLastFeedItem
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.Utils
import com.topface.topface.utils.adapter_utils.DividerDecoration
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

/**
 * Новый дейтинг фрагмент с симпатиями и восхищениями
 * Created by tiberal on 30.11.16.
 */
@FlurryOpenEvent(name = PAGE_NAME)
class DialogsFragment : BaseFragment(), IBannerAds {

    companion object {
        const val PAGE_NAME = "Dialogs"
    }

    private lateinit var mBannersController: BannersController
    private val mFeedRequestFactory by lazy {
        FeedRequestFactory(context)
    }
    private val mApi by lazy {
        FeedApi(context, this, mFeedRequestFactory = mFeedRequestFactory)
    }
    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }
    private val mBinding: DialogsFragmentLayoutBinding by lazy {
        DataBindingUtil.inflate<DialogsFragmentLayoutBinding>(context.layoutInflater, R.layout.dialogs_fragment_layout, null, false)
    }
    private val mDialogTypeProvider by lazy {
        DialogTypeProvider()
    }

    private val mDecorator by lazy {
        DividerDecoration(R.color.filter_divider_color.getColor(), R.dimen.divider_size.getDimen()
                , R.dimen.avatar_marginLeft.getDimen() + R.dimen.avatar_marginRight.getDimen() + R.dimen.avatar_width.getDimen())
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mDialogTypeProvider) {
            Bundle().apply {
                val last = mAdapter.data.findLastFeedItem()
                putString(FeedRequestFactory.TO, if (last != null) last.id else Utils.EMPTY)
            }
        }
                .addAdapterComponent(AppDayItemComponent(mApi, InstantPurchaseModel(mNavigator, PAGE_NAME)))
                .addAdapterComponent(DialogItemComponent(mNavigator))
                .addAdapterComponent(EmptyDialogsComponent())
                .addAdapterComponent(EmptyDialogsFragmentComponent(mNavigator))
                .addAdapterComponent(activity.registerLifeCycleDelegate(ContactsItemComponent(mNavigator, context.applicationContext, mApi)))
    }
    private val mViewModel by lazy {
        DialogsFragmentViewModel(context, mApi) { mAdapter.updateObservable }.apply {
            activity.registerLifeCycleDelegate(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        mBinding.viewModel = mViewModel
        mBannersController = BannersController(this)
        activity.registerLifeCycleDelegate(mBannersController)
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        ToolbarManager.setToolbarSettings(ToolbarSettingsData(getString(R.string.settings_messages)))
    }

    override fun onDetach() {
        super.onDetach()
        activity.unregisterLifeCycleDelegate(mAdapter.components.values)
        activity.unregisterLifeCycleDelegate(mViewModel)
        activity.unregisterLifeCycleDelegate(mBannersController)
    }

    override fun onDestroyView() {
        mBinding.dialogsList.stopScroll()
        super.onDestroyView()
        /**
         * https://code.google.com/p/android/issues/detail?id=78062
         * увидел, промониторь изменения и обнови дату/поправь
         * 14.12.16
         */
        with(mBinding.refresh) {
            destroyDrawingCache()
            clearAnimation()
        }
        mViewModel.release()
        mAdapter.releaseComponents()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ChatActivity.REQUEST_CHAT) {
            data?.let {
                if (data.getBooleanExtra(ChatFragment.MUTUAL, false) && RateAppFragment.isApplicable(App.get().options.ratePopupNewVersion)) {
                    mNavigator.showRateAppFragment()
                }
            }
        }
    }

    private fun initList() = with(mBinding.dialogsList) {
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(mDecorator)
        adapter = mAdapter
    }

    override fun getContainerForAd() = mBinding.bannerContainerForFeeds as ViewGroup

}