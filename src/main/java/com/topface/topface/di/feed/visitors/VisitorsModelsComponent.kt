package com.topface.topface.di.feed.visitors

import com.topface.topface.di.AppComponent
import com.topface.topface.di.Immortal
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorsViewModel
import dagger.Component

@Component(modules = arrayOf(VisitorsModelsModule::class), dependencies = arrayOf(AppComponent::class))
@ScreenScope
interface VisitorsModelsComponent : Immortal {
    fun visitorsViewModel(): VisitorsViewModel
}