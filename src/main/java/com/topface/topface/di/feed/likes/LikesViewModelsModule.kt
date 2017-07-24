package com.topface.topface.di.feed.likes

import com.topface.topface.api.Api
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.LikesViewModel
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import dagger.Module
import dagger.Provides

@Module
class LikesViewModelsModule {

    @Provides
    @ScreenScope
    fun provideLikesViewModel(api: Api): LikesViewModel =
            LikesViewModel(api)
}