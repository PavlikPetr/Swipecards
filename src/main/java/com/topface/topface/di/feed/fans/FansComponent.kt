package com.topface.topface.di.feed.fans

import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansAdapterComponent
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansFragment
import dagger.Subcomponent

/**
 * Created by tiberal on 22.02.17.
 */
@FragmentScope
@Subcomponent(modules = arrayOf(FansModule::class))
interface FansComponent {
    fun inject(fragment: FansFragment)
    fun inject(fansAdapterComponent: FansAdapterComponent)
}