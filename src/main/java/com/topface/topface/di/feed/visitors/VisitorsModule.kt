package com.topface.topface.di.feed.visitors

import android.databinding.ViewStubProxy
import com.topface.topface.R
import com.topface.topface.data.Visitor
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorsFragment
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorsLockController
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorsLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.MultiselectionController
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import dagger.Module
import dagger.Provides

/**
 * Created by tiberal on 12.02.17.
 */

@Module(includes = arrayOf(BaseFeedModule::class))
class VisitorsModule(private val mFragment: VisitorsFragment) {

    val emptyFeedLayout: Int = R.layout.layout_empty_visitors

    @Provides
    @FragmentScope
    fun providesMultiselectionController(): MultiselectionController<Visitor> {
        return MultiselectionController(mFragment)
    }

    @Provides
    @FragmentScope
    fun provideTypeProvider() = object : ITypeProvider {
        override fun getType(java: Class<*>) = when (java) {
            Visitor::class.java -> 1
            else -> 0
        }
    }

    @Provides
    @FragmentScope
    fun providesLockScreenVMFactory(navigator: IFeedNavigator): BaseFeedLockerController.ILockScreenVMFactory =
            object : BaseFeedLockerController.ILockScreenVMFactory {
                override fun construct() = VisitorsLockScreenViewModel(navigator, mIFeedUnlocked = mFragment)
            }

    @Provides
    @FragmentScope
    fun providesVisitorsLockController(lockerFactory: BaseFeedLockerController.ILockScreenVMFactory): BaseFeedLockerController<*> {
        return VisitorsLockController(mFragment.mBinding.emptyFeedStub as ViewStubProxy, mShower = mFragment).apply {
            lockScreenFactory = lockerFactory
            setLockerLayout(emptyFeedLayout)
        }
    }
}

