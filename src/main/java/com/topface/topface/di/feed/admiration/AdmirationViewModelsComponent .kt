package com.topface.topface.di.feed.admiration

import com.topface.topface.di.AppComponent
import com.topface.topface.di.Immortal
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration.AdmirationViewModel
import dagger.Component

@Component(modules = arrayOf(AdmirationViewModelsModule::class), dependencies = arrayOf(AppComponent::class))
@ScreenScope
interface AdmirationViewModelsComponent : Immortal {
    fun admirationViewModel(): AdmirationViewModel
}