package com.topface.topface.ui.fragments.feed.fans

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.R
import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.LayoutEmptyFansBinding
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

@FlurryOpenEvent(name = FansFragment.SCREEN_TYPE)
class FansFragment : BaseFeedFragment<FeedBookmark, LayoutEmptyFansBinding>() {

    companion object {
        const val SCREEN_TYPE = "Fans"
    }


    override val mViewModel by lazy {
        FansFragmentViewModel(mBinding, mNavigator, mApi)
    }
    override val mLockerControllerBase by lazy {
        FansLockController(mBinding.emptyFeedStub as ViewStubProxy)
    }
    override val mAdapter by lazy {
        FansAdapter(mNavigator)
    }

    override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyFansBinding> {
        override fun construct(binding: ViewDataBinding) = FansLockScreenViewModel(binding as LayoutEmptyFansBinding, mNavigator, this@FansFragment)
    }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_fans

    override fun getTitle(): String = getString(R.string.general_visitors)

}
