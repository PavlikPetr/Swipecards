package com.topface.topface.di.navigation_activity

import android.content.Context
import com.topface.topface.di.scope.ActivityScope
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import dagger.Module
import dagger.Provides

/**
 * Created by tiberal on 08.02.17.
 */

@Module
class NavigationActivityModule(private val mActivity: NavigationActivity) {

    @Provides
    @ActivityScope
    fun provideDeleteFeedRequestFactory(context: Context) = DeleteFeedRequestFactory(context)

    @Provides
    @ActivityScope
    fun provideFeedRequestFactory(context: Context) = FeedRequestFactory(context)

    @Provides
    @ActivityScope
    fun provideFeedApi(context: Context, deleteFeedRequestFactory: DeleteFeedRequestFactory,
                       feedRequestFactory: FeedRequestFactory)
            = FeedApi(context, mActivity, deleteFeedRequestFactory, feedRequestFactory)

    @Provides
    @ActivityScope
    fun provideNavigator(): IFeedNavigator = FeedNavigator(mActivity)
}
