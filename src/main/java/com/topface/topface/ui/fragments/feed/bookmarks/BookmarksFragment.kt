package com.topface.topface.ui.fragments.feed.bookmarks

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.R
import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.LayoutEmptyBookmarksBinding
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Created by tiberal on 19.09.16.
 */
@FlurryOpenEvent(name = BookmarksFragment.PAGE_NAME)
class BookmarksFragment : BaseFeedFragment<FeedBookmark, LayoutEmptyBookmarksBinding>() {

    companion object {
        const val PAGE_NAME = "Bookmarks"
    }

    override val mViewModel by lazy {
        BookmarksFragmentViewModel(mBinding, mNavigator, mApi)
    }
    override val mLockerControllerBase by lazy {
        BookmarksLockScreenController(mBinding.emptyFeedStub as ViewStubProxy)
    }
    override val mAdapter by lazy {
        BookmarksAdapter(mNavigator)
    }

    override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyBookmarksBinding> {
        override fun construct(binding: ViewDataBinding) = BookmarksLockScreenViewModel(binding as LayoutEmptyBookmarksBinding, mNavigator)
    }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_bookmarks

    override fun onResume() {
        super.onResume()
        mViewModel.onResume()
    }
}