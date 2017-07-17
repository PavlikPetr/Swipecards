package com.topface.topface.di.feed.mutual

import com.topface.topface.di.AppComponent
import com.topface.topface.di.Immortal
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual.MutualViewModel
import dagger.Component

@Component(modules = arrayOf(MutualViewModelsModule::class), dependencies = arrayOf(AppComponent::class))
@ScreenScope
interface MutualViewModelsComponent : Immortal {
    fun mutualViewModel(): MutualViewModel
}