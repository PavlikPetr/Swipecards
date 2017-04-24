package com.topface.topface.ui.fragments.feed.enhanced.base

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appodeal.ads.g
import com.topface.topface.R
import com.topface.topface.banners.IPageWithAds
import com.topface.topface.banners.PageInfo
import com.topface.topface.data.FeedItem
import com.topface.topface.databinding.NewFeedFragmentBaseBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.ActionModeController
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.ui.fragments.feed.feed_base.MultiselectionController
import com.topface.topface.ui.fragments.feed.feed_base.MultiselectionController.IMultiSelectionListener
import com.topface.topface.ui.fragments.feed.feed_utils.getFeedIdList
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

/**
 * Базовый фрагментик для всех фидов
 * Created by tiberal on 10.02.17.
 */
abstract class BaseFeedFragment<T : FeedItem> : BaseFragment(), IMultiSelectionListener,
        ActionModeController.OnActionModeEventsListener,
        IFeedUnlocked, IPageWithAds {
    /**
     * onSaveInstanceStateWasCalled - флаг того, что был вызван onSaveInstanceState, следовательно
     * мы или пересоздаем фрагмент либо свернули приложение
     */
    private var onSaveInstanceStateWasCalled = false
    /**
     * onStartAfterSavedStateWasCalled - флаг того, что был дернут onStart после onSaveInstanceState
     * значит мы развернули приложение после сворачивания
     */
    private var onStartAfterSavedStateWasCalled = false
    protected open val res: Int = R.layout.new_feed_fragment_base
    val mBinding: NewFeedFragmentBaseBinding by lazy {
        DataBindingUtil.inflate<NewFeedFragmentBaseBinding>(context.layoutInflater, res, null, false)
    }
    open val actionModeMenu = R.menu.feed_context_menu

    @Inject lateinit var mMultiselectionController: MultiselectionController<T>
    @Inject lateinit var mApi: FeedApi
    @Inject lateinit var mNavigator: IFeedNavigator
    @Inject lateinit var mActionModeController: ActionModeController
    @Inject lateinit var mAdapter: CompositeAdapter
    @Inject lateinit var mLockerControllerBase: BaseFeedLockerController<*>

    abstract val mViewModel: BaseFeedFragmentModel<T>


    abstract fun attachAdapterComponents(compositeAdapter: CompositeAdapter)

    @Suppress("UNCHECKED_CAST")
    protected fun itemLongClick(view: View?) {
        val itemPosition = mBinding.feedList.layoutManager.getPosition(view)
        val data = mAdapter.data[itemPosition] as T
        if (!mActionModeController.isActionModeEnabled() && view != null && activity is AppCompatActivity) {
            (activity as AppCompatActivity).startSupportActionMode(mActionModeController)
            mMultiselectionController.startMultiSelection()
            mMultiselectionController.handleSelected(data, view, itemPosition)
            mAdapter.notifyItemChanged(itemPosition)
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun itemClick(view: View?) {
        val itemPosition = mBinding.feedList.layoutManager.getPosition(view)
        val data = mAdapter.data[itemPosition] as T
        if (mActionModeController.isActionModeEnabled() && view != null) {
            mMultiselectionController.handleSelected(data, view, itemPosition)
            mAdapter.notifyItemChanged(itemPosition)
        } else {
            mViewModel.itemClick(view, itemPosition, data)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel.apply {
            navigator = mNavigator
            stubView = mLockerControllerBase
        } as BaseFeedFragmentModel<FeedItem>
        mAdapter.apply {
            attachAdapterComponents(this)
            mViewModel.updateObservable = updateObservable
        }
        initScreenView(mBinding)
        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        if (onSaveInstanceStateWasCalled) {
            onStartAfterSavedStateWasCalled = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (onSaveInstanceStateWasCalled) {
            /**
             * Если onSaveInstanceState вызвался и флаг onSaveInstanceStateWasCalled уже в true
             * значит фрагмент пересоздается после того, как мы развернули приложение и
             * onStartAfterSavedStateWasCalled нужно скинуть чтоб ViewModel не померла
             */
            onStartAfterSavedStateWasCalled = false
        } else {
            /**
             * Пересоздаем фрагмент ViewModel убивать не нужно
             */
            onSaveInstanceStateWasCalled = true
        }
    }

    override fun onDetach() {
        /**
         * Рутовый фрагмент с табами, костыльный чуть более чем полностью. При повороте создается
         * и уничтоается лишний инстанс фрагмента. По этому терминальный ивент для компонента
         * даггера кидаем только для видимого фрагмента.
         * Вообщем это адуха ребята
         * Почитай описание используемых флажков выше
         * 1) onSaveInstanceStateWasCalled && onStartAfterSavedStateWasCalled - было сохранение в стейт
         * и был вызов onStart после сохранения в стейт. Следовательно приложение было свернуто и развернуто
         * 2) !onSaveInstanceStateWasCalled - сохранения в стейт не было, следовательно мы уходи из фрагмента
         */
        if (isAdded && ((onSaveInstanceStateWasCalled && onStartAfterSavedStateWasCalled) || !onSaveInstanceStateWasCalled)) {
            terminateImmortalComponent()
            mViewModel.release()
        }
        super.onDetach()
        mViewModel.unbind()
        mBinding.unbind()
    }

    protected open fun terminateImmortalComponent() {
    }

    protected fun initScreenView(binding: NewFeedFragmentBaseBinding) {
        with(binding.feedList) {
            itemAnimator = null
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    override fun onDeleteFeedItems() {
        mViewModel.onDeleteFeedItems(mMultiselectionController.mSelected,
                getDeleteItemsList(mMultiselectionController.mSelected))
    }

    override fun onAddToBlackList() {
        mViewModel.onAddToBlackList(mMultiselectionController.mSelected)
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
    }

    override fun onSelected(size: Int) = mActionModeController.setSelectedCount(size)

    override fun onFeedUnlocked() = mViewModel.update(force = true)

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser && isAdded) {
            mActionModeController.finishIfEnabled()
        }
    }

    open fun getDeleteItemsList(mSelected: MutableList<T>) = mSelected.getFeedIdList()

    override fun onDestroyView() {
        (mBinding.bannerContainerForFeeds as ViewGroup).removeViewInLayout(g.v)
        super.onDestroyView()
        //пока пусть будет так, похоже что это лишнее, на дестрое вьюхи не надо релизить модель.
        //mViewModel.release()
        mAdapter.releaseComponents()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionModeController.finishIfEnabled()
        mLockerControllerBase.release()
    }

    override fun getPageName() = PageInfo.PageName.VISITORS_TABS

    override fun getContainerForAd() = mBinding.bannerContainerForFeeds as ViewGroup
}