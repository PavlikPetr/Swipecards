package com.topface.topface.di.chat

import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatActivity
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import dagger.Subcomponent


@FragmentScope
@Subcomponent(modules = arrayOf(ChatModule::class))
interface ChatComponent {
    fun inject(fragment: ChatActivity)
    fun inject(fragment: ChatFragment)
    fun feedNavigator(): FeedNavigator
}