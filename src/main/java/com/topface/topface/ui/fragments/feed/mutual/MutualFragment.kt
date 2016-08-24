package com.topface.topface.ui.fragments.feed.mutual

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.R
import com.topface.topface.data.FeedMutual
import com.topface.topface.databinding.LayoutEmptyMutualBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController
import com.topface.topface.viewModels.BaseViewModel

/**
 * Фрагмент взаимных симпатий
 * Created by tiberal on 01.08.16.
 */
class MutualFragment : BaseFeedFragment<FeedMutual, LayoutEmptyMutualBinding>() {

    override val mViewModel by lazy {
        MutualFragmentViewModel(mBinding, mNavigator, mApi)
    }
    override val mLockerControllerBase by lazy {
        MutualLockController(mBinding.emptyFeedStub as ViewStubProxy)
    }
    override val mAdapter by lazy {
        MutualFeedAdapter(mNavigator)
    }

    override fun createLockerFactory(): BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyMutualBinding> =
            object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyMutualBinding> {
                override fun construct(binding: ViewDataBinding): BaseViewModel<LayoutEmptyMutualBinding> {
                    return MutualLockScreenViewModel(binding as LayoutEmptyMutualBinding, mNavigator, this@MutualFragment)
                }
            }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_mutual
}