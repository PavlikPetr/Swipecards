package com.topface.topface.di.feed.fans

import android.databinding.ViewStubProxy
import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.data.FeedItem
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansFragment
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansLockController
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.MultiselectionController
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import dagger.Module
import dagger.Provides

@Module(includes = arrayOf(BaseFeedModule::class))
class FansModule(private val mFragment: FansFragment) {

    val emptyFeedLayout = R.layout.layout_empty_fans

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
                override fun construct() = FansLockScreenViewModel(mFragment)
            }

    @Provides
    @FragmentScope
    fun providesVisitorsLockController(lockerFactory: BaseFeedLockerController.ILockScreenVMFactory, navigator: IFeedNavigator)
            : BaseFeedLockerController<*> {
        return FansLockController(mFragment.mBinding.emptyFeedStub as ViewStubProxy, navigator).apply {
            lockScreenFactory = lockerFactory
            setLockerLayout(emptyFeedLayout)
        }
    }


    @Provides
    @FragmentScope
    fun provideAdapter(typeProvider: ITypeProvider) = CompositeAdapter(typeProvider) {
        Bundle().apply {
            if (it.data.isNotEmpty()) {
                putString(FeedRequestFactory.TO, (it.data.last() as FeedItem).id)
            }
        }
    }
}