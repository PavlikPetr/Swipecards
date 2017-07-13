package com.topface.topface.di.feed.mutual

import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual.MutualAdapterComponent
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual.MutualFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = arrayOf(MutualModule::class))
interface MutualComponent {
    fun inject(fragment: MutualFragment)
    fun inject(mutualAdapterComponent: MutualAdapterComponent)
}