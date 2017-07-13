package com.topface.topface.di.navigation_activity

import com.topface.topface.di.chat.ChatComponent
import com.topface.topface.di.chat.ChatModule
import com.topface.topface.di.feed.admiration.AdmirationComponent
import com.topface.topface.di.feed.admiration.AdmirationModule
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.feed.fans.FansComponent
import com.topface.topface.di.feed.fans.FansModule
import com.topface.topface.di.feed.mutual.MutualComponent
import com.topface.topface.di.feed.mutual.MutualModule
import com.topface.topface.di.feed.visitors.VisitorsComponent
import com.topface.topface.di.feed.visitors.VisitorsModule
import com.topface.topface.di.scope.ActivityScope
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = arrayOf(NavigationActivityModule::class))
interface NavigationActivityComponent {

    fun api(): FeedApi
    fun navigator(): IFeedNavigator

    fun inject(activity: NavigationActivity)

    fun add(visitorsModule: VisitorsModule, baseModule: BaseFeedModule): VisitorsComponent
    fun add(fansModule: FansModule, baseModule: BaseFeedModule): FansComponent
    fun add(chatModule: ChatModule): ChatComponent

    fun add(mutualModule: MutualModule, baseModule: BaseFeedModule): MutualComponent
    fun add(admirationModule: AdmirationModule, baseModule: BaseFeedModule): AdmirationComponent
}
