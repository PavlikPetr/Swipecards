package com.topface.topface.di.feed.fans

import com.topface.topface.di.AppComponent
import com.topface.topface.di.Immortal
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansViewModel
import dagger.Component

@Component(modules = arrayOf(FansViewModelsModule::class), dependencies = arrayOf(AppComponent::class))
@ScreenScope
interface FansViewModelsComponent : Immortal {
    fun fansViewModel(): FansViewModel
}