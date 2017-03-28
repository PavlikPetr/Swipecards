package com.topface.topface.di.feed.visitors

import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorsFragment
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorAdapterComponent
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = arrayOf(VisitorsModule::class))
interface VisitorsComponent {
    fun inject(fragment: VisitorsFragment)
    fun inject(visitorAdapterComponent: VisitorAdapterComponent)
}