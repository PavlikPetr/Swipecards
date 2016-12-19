package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.banners.BannersController
import com.topface.topface.banners.IPageWithAds
import com.topface.topface.banners.PageInfo
import com.topface.topface.databinding.DialogsFragmentLayoutBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.AppDayItemComponent
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.ContactsItemComponent
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.DialogItemComponent
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.EmptyDialogsComponent
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.EmptyDialogsFragmentComponent
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.findLastFeedItem
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.Utils
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

/**
 * Новый дейтинг прагмент с симпатиями и восхищениями
 * Created by tiberal on 30.11.16.
 */
class DialogsFragment : BaseFragment(), IPageWithAds {
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
    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mDialogTypeProvider) {
            Bundle().apply {
                val last = mAdapter.data.findLastFeedItem()
                putString(FeedRequestFactory.TO, if (last != null) last.id else Utils.EMPTY)
            }
        }
                .addAdapterComponent(AppDayItemComponent(mApi))
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.release()
        mAdapter.releaseComponents()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mViewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun initList() = with(mBinding.dialogsList) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBannersController = BannersController(this, App.get().options)
    }

    override fun getPageName() = PageInfo.PageName.MESSAGES_TABS

    override fun getContainerForAd() = mBinding.bannerContainerForFeeds as ViewGroup

}