package com.topface.topface.ui.fragments.feed.likes

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.LayoutEmptyLikesBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedLockerController
import com.topface.topface.viewModels.BaseViewModel

/**
 * Фрагмент симпатий
 * Created by tiberal on 01.08.16.
 */
class LikesFragment : FeedFragment<FeedLike, LayoutEmptyLikesBinding>() {

    override fun createLockerFactory(): FeedLockerController.ILockScreenVMFactory<LayoutEmptyLikesBinding> =
            object : FeedLockerController.ILockScreenVMFactory<LayoutEmptyLikesBinding> {
                override fun construct(binding: ViewDataBinding): BaseViewModel<LayoutEmptyLikesBinding> {
                    return LikesLockScreenViewModel(binding as LayoutEmptyLikesBinding, mApi, mNavigator, App.get().dataUpdater, this@LikesFragment)
                }
            }

    override val mAdapter by lazy {
        LikesFeedAdapter(mNavigator, mApi)
    }
    override val mViewModel by lazy {
        LikesFragmentViewModel(mBinding, mNavigator, mApi)
    }

    override val mLockerController by lazy {
        LikesLockController(mBinding.emptyFeedStub as ViewStubProxy)
    }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_likes

}