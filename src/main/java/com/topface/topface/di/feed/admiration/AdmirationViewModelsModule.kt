package com.topface.topface.di.feed.admiration

import com.topface.topface.api.Api
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration.AdmirationViewModel
import dagger.Module
import dagger.Provides

@Module
class AdmirationViewModelsModule {

    @Provides
    @ScreenScope
    fun provideAdmirationViewModel(api: Api): AdmirationViewModel =
            AdmirationViewModel(api)
}