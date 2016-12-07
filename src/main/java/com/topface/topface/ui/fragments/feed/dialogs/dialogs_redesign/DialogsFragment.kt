package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.DialogsFragmentLayoutBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.ContactsItemComponent
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.DialogItemComponent
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components.EmptyDialogsComponent
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.findLastFeedItem
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.IStateSaverRegistrator
import com.topface.topface.utils.Utils
import org.jetbrains.anko.layoutInflater

/**
 * Новый дейтинг прагмент с симпатиями и восхищениями
 * Created by tiberal on 30.11.16.
 */
class DialogsFragment : BaseFragment() {

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
                .addAdapterComponent(DialogItemComponent(mNavigator))
                .addAdapterComponent(EmptyDialogsComponent())
                .addAdapterComponent(ContactsItemComponent(mNavigator, context.applicationContext), activity as IStateSaverRegistrator)
    }
    private val mViewModel by lazy {
        DialogsFragmentViewModel(context, mApi) { mAdapter.updateObservable }.apply {
            (activity as IStateSaverRegistrator).registerLifeCycleDelegate(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.release()
    }

    private fun initList() = with(mBinding.dialogsList) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
    }

}