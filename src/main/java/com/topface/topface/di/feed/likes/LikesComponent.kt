package com.topface.topface.di.feed.likes

import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.LikeFragment
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual.MutualAdapterComponent
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = arrayOf(LikesModule::class))
interface LikesComponent {
    fun inject(fragment: LikeFragment)
    fun inject(mutualAdapterComponent: MutualAdapterComponent)
}