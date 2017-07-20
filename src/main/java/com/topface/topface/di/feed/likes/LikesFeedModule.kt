package com.topface.topface.di.feed.likes

import android.os.Bundle
import com.topface.topface.data.FeedItem
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.LikesAdapter
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.new_adapter.enhanced.IAdapter
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import dagger.Module
import dagger.Provides

@Module
class LikesFeedModule(private val mFragment: BaseFeedFragment<*, *, *>) : BaseFeedModule(mFragment) {

    @Provides
    @FragmentScope
    fun provideAdapter():IAdapter = LikesAdapter()

//    @Provides
//    @FragmentScope
//    fun provideAdapter(typeProvider: ITypeProvider)= CompositeAdapter(typeProvider) {
//        Bundle().apply {
//            if (it.data.isNotEmpty()) {
//                putString(FeedRequestFactory.TO, (it.data.last() as FeedItem).id)
//            }
//        }
//    }
}