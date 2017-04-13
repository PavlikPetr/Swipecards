package com.topface.topface.ui.fragments.feed.blacklist

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.R
import com.topface.topface.data.BlackListItem
import com.topface.topface.databinding.LayoutEmptyBlacklistBinding
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_utils.convertFeedIdList
import com.topface.topface.ui.fragments.feed.feed_utils.getUserIdList
import java.util.*

@FlurryOpenEvent(name = BlackListFragment.PAGE_NAME)
class BlackListFragment : BaseFeedFragment<BlackListItem, LayoutEmptyBlacklistBinding>() {
    override fun getDeleteItemsList(mSelected: MutableList<BlackListItem>): ArrayList<String> {
        return mSelected.getUserIdList().convertFeedIdList()
    }

    companion object {
        const val PAGE_NAME = "blacklist"
    }

    override val mViewModel by lazy {
        BlackListFragmentViewModel(mBinding, mNavigator, mApi)
    }
    override val mLockerControllerBase by lazy {
        BlackListLockScreenController(mBinding.emptyFeedStub as ViewStubProxy)
    }
    override val mAdapter by lazy {
        BlackListAdapter(mNavigator)
    }

    override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyBlacklistBinding> {
        override fun construct(binding: ViewDataBinding) = BlackListLockScreenViewModel(binding as LayoutEmptyBlacklistBinding, mNavigator)
    }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_blacklist

    override fun getActionModeMenu() = R.menu.feed_context_menu_blacklist
}