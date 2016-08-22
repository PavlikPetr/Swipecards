package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.banners.BannersController
import com.topface.topface.banners.IPageWithAds
import com.topface.topface.banners.PageInfo
import com.topface.topface.data.FeedItem
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.ui.adapters.ItemEventListener
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.extensions.inflateBinding

/**
 * Новый, и усосвершенствованный FeedFragment
 * Created by tiberal on 01.08.16.
 */
abstract class BaseFeedFragment<T : FeedItem, V : ViewDataBinding> :
        BaseFragment(), MultiselectionController.IMultiSelectionListener,
        ActionModeController.OnActionModeEventsListener<T>,
        ItemEventListener.OnRecyclerViewItemLongClickListener<T>,
        ItemEventListener.OnRecyclerViewItemClickListener<T>,
        IFeedUnlocked, IPageWithAds {

    open val res: Int = R.layout.fragment_feed_base
    protected val mBinding by inflateBinding<FragmentFeedBaseBinding>(res)
    private lateinit var mBannersController: BannersController
    private val mDelRequestFactory by lazy {
        DeleteFeedRequestFactory(context)
    }
    private val mFeedRequestFactory by lazy {
        FeedRequestFactory(context)
    }
    protected val mApi by lazy {
        FeedApi(context, this, mDelRequestFactory, mFeedRequestFactory)
    }
    private val mActionModeController by lazy {
        ActionModeController(activity.menuInflater, this)
    }
    private val mMultiselectionController by lazy {
        MultiselectionController<T>(this)
    }
    protected val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    abstract val mViewModel: FeedFragmentViewModel<T>
    abstract val mLockerController: FeedLockerController<V, *>
    abstract val mAdapter: BaseFeedAdapter<T>
    abstract fun createLockerFactory(): FeedLockerController.ILockScreenVMFactory<V>
    abstract fun getEmptyFeedLayout(): Int

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initScreenView(mBinding)
        mBinding.viewModel = mViewModel as FeedFragmentViewModel<FeedItem>
        setupLocker()
        return mBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBannersController = BannersController(this, App.get().options)
    }

    protected fun initScreenView(binding: FragmentFeedBaseBinding) {
        with(binding.feedList) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter.setOnItemLongClickListener(this)
        mAdapter.setOnItemClickListener(this)
    }

    protected fun setupLocker() {
        mViewModel.stubView = mLockerController
        mLockerController.lockScreenFactory = createLockerFactory()
        mLockerController.setLockerLayout(getEmptyFeedLayout())
    }

    override fun onDeleteFeedItems() {
        mViewModel.onDeleteFeedItems(mMultiselectionController.mSelected)
    }

    override fun onAddToBlackList() {
        mViewModel.onAddToBlackList(mMultiselectionController.mSelected)
    }

    override fun onSetToolbarVisibility(visibility: Boolean) {
    }

    override fun onActionModeFinish() {
        mMultiselectionController.stopMultiSelection()
        mAdapter.notifyDataSetChanged()
        mAdapter.isNeedHighLight = null
    }

    override fun onSelected(size: Int) = mActionModeController.setTitle(size)

    override fun itemLongClick(view: View?, itemPosition: Int, data: T?) {
        if (!mActionModeController.isActionModeEnabled() && data != null && view != null) {
            if (activity is AppCompatActivity) {
                (activity as AppCompatActivity).startSupportActionMode(mActionModeController)
                mMultiselectionController.startMultiSelection()
                mMultiselectionController.handleSelected(data, view)
                mAdapter.isNeedHighLight = { data -> mMultiselectionController.mSelected.contains(data) }
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun itemClick(view: View?, itemPosition: Int, data: T?) {
        if (mActionModeController.isActionModeEnabled() && data != null && view != null) {
            mMultiselectionController.handleSelected(data, view)
            mAdapter.notifyDataSetChanged()
        } else {
            mViewModel.itemClick(view, itemPosition, data)
        }
    }

    override fun onFeedUnlocked() {
        mViewModel.update()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mActionModeController.isActionModeEnabled()) {
            mActionModeController.finish()
        }
        mViewModel.release()
    }

    override fun getPageName() = PageInfo.PageName.UNKNOWN_PAGE

    override fun getContainerForAd(): ViewGroup? = view?.findViewById(R.id.banner_container_for_feeds) as ViewGroup

}
