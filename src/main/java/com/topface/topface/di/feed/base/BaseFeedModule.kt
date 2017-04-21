package com.topface.topface.di.feed.base

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.banners.BannersController
import com.topface.topface.data.FeedItem
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.ActionModeController
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import dagger.Module
import dagger.Provides

@Module
class BaseFeedModule(private val mFragment: BaseFeedFragment<*>) {

    private lateinit var mAdapter: CompositeAdapter

    @Provides
    @FragmentScope
    fun provideActionModeController() =
            ActionModeController(mFragment.activity.menuInflater, mFragment.actionModeMenu,
                    mActionModeEventsListener = mFragment)

    @Provides
    @FragmentScope
    fun provideBannersController() = BannersController(mFragment, App.get().options)

    @Provides
    @FragmentScope
    fun provideCompositeAdapter(typeProvider: ITypeProvider): CompositeAdapter {
        mAdapter = CompositeAdapter(typeProvider) {
            Bundle().apply {
                if (mAdapter.data.isNotEmpty()) {
                    putString(FeedRequestFactory.TO, (mAdapter.data.last() as FeedItem).id)
                }
            }
        }
        return mAdapter
    }
}