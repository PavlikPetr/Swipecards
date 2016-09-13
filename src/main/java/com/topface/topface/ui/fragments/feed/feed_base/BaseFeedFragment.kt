package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flurry.sdk.it
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
 * @param T - feed item type
 * @param V - empty screen binding class
 */
abstract class BaseFeedFragment<T : FeedItem, V : ViewDataBinding> :
        BaseFragment(), MultiselectionController.IMultiSelectionListener,
        ActionModeController.OnActionModeEventsListener,
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

    abstract val mViewModel: BaseFeedFragmentViewModel<T>
    abstract val mLockerControllerBase: BaseFeedLockerController<V, *>
    abstract val mAdapter: BaseFeedAdapter<*, T>
    abstract fun createLockerFactory(): BaseFeedLockerController.ILockScreenVMFactory<V>
    abstract fun getEmptyFeedLayout(): Int

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initScreenView(mBinding)
        mBinding.viewModel = mViewModel as BaseFeedFragmentViewModel<FeedItem>
        setupLocker()
        return mBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBannersController = BannersController(this, App.get().options)
    }

    protected fun initScreenView(binding: FragmentFeedBaseBinding) {
        with(binding.feedList) {
            itemAnimator = null
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter.setOnItemLongClickListener(this)
        mAdapter.setOnItemClickListener(this)
    }

    protected fun setupLocker() {
        mViewModel.stubView = mLockerControllerBase
        mLockerControllerBase.lockScreenFactory = createLockerFactory()
        mLockerControllerBase.setLockerLayout(getEmptyFeedLayout())
    }

    override fun onDeleteFeedItems() {
        if (mMultiselectionController.mSelected.size > 0) {
            mViewModel.onDeleteFeedItems(mMultiselectionController.mSelected)
        } else {
            onActionModeFinish()
        }
    }

    override fun onAddToBlackList() {
        if (mMultiselectionController.mSelected.size > 0) {
            mViewModel.onAddToBlackList(mMultiselectionController.mSelected)
        } else {
            onActionModeFinish()
        }
    }

    override fun onSetToolbarVisibility(visibility: Boolean) {
    }

    override fun onActionModeFinish() {
        mMultiselectionController.stopMultiSelection()
        mMultiselectionController.mSelectedItemsPositions.filter {
            it < mAdapter.data.size
        }.map {
            mAdapter.notifyItemChanged(it)
        }
        mAdapter.isNeedHighLight = null
    }

    override fun onSelected(size: Int) = mActionModeController.setTitle(size)

    override fun itemLongClick(view: View?, itemPosition: Int, data: T?) =
            if (!mActionModeController.isActionModeEnabled() && data != null && view != null && activity is AppCompatActivity) {
                (activity as AppCompatActivity).startSupportActionMode(mActionModeController)
                mMultiselectionController.startMultiSelection()
                mMultiselectionController.handleSelected(data, view, itemPosition)
                mAdapter.isNeedHighLight = { data -> mMultiselectionController.mSelected.contains(data) }
                mAdapter.notifyItemChanged(itemPosition)
            } else Unit


    override fun itemClick(view: View?, itemPosition: Int, data: T?) =
            if (mActionModeController.isActionModeEnabled() && data != null && view != null) {
                mMultiselectionController.handleSelected(data, view, itemPosition)
                mAdapter.notifyItemChanged(itemPosition)
            } else {
                mViewModel.itemClick(view, itemPosition, data)
            }

    override fun onFeedUnlocked() = mViewModel.update()

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser && isAdded) {
            mActionModeController.finishIfEnabled()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionModeController.finishIfEnabled()
        mViewModel.release()
    }

    override fun getPageName() = PageInfo.PageName.UNKNOWN_PAGE

    override fun getContainerForAd() = view?.findViewById(R.id.banner_container_for_feeds) as ViewGroup

}
