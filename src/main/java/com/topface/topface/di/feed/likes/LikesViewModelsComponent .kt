package com.topface.topface.di.feed.likes

import com.topface.topface.di.AppComponent
import com.topface.topface.di.Immortal
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.LikesViewModel
import dagger.Component

@Component(modules = arrayOf(LikesViewModelsModule::class), dependencies = arrayOf(AppComponent::class))
@ScreenScope
interface LikesViewModelsComponent : Immortal {
    fun likesViewModel(): LikesViewModel
}