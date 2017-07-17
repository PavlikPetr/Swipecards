package com.topface.topface.di.feed.admiration

import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration.AdmirationAdapterComponent
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration.AdmirationFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = arrayOf(AdmirationModule::class))
interface AdmirationComponent {
    fun inject(fragment: AdmirationFragment)
    fun inject(admirationsAdapterComponent: AdmirationAdapterComponent)
}