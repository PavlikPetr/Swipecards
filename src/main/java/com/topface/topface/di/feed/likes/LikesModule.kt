package com.topface.topface.di.feed.likes

import android.databinding.ViewStubProxy
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.LikeFragment
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.LikesLockController
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.LikesLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.MultiselectionController
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import dagger.Module
import dagger.Provides

@Module(includes = arrayOf(BaseFeedModule::class))
class LikesModule(private val mFragment: LikeFragment) {

    val emptyFeedLayout = R.layout.base_sympathy_stub_layout

    @Provides
    @FragmentScope
    fun providesMultiselectionController(): MultiselectionController<FeedBookmark> {
        return MultiselectionController(mFragment)
    }

    @Provides
    @FragmentScope
    fun provideTypeProvider() = object : ITypeProvider {
        override fun getType(java: Class<*>) = when (java) {
            FeedBookmark::class.java -> 1
            else -> 0
        }
    }

    @Provides
    @FragmentScope
    fun providesLockScreenVMFactory(): BaseFeedLockerController.ILockScreenVMFactory =
            object : BaseFeedLockerController.ILockScreenVMFactory {
                override fun construct() = LikesLockScreenViewModel(mFragment)
            }

    @Provides
    @FragmentScope
    fun providesVisitorsLockController(lockerFactory: BaseFeedLockerController.ILockScreenVMFactory, navigator: IFeedNavigator)
            : BaseFeedLockerController<*> {
        //TODO mFragment.mBinding.emptyFeedStub as ViewStubProxy
        return LikesLockController(mFragment.mBinding as ViewStubProxy, navigator).apply {
            lockScreenFactory = lockerFactory
            setLockerLayout(emptyFeedLayout)
        }
    }

}