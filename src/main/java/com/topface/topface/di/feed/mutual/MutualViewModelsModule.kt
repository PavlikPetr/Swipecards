package com.topface.topface.di.feed.mutual

import com.topface.topface.api.Api
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual.MutualViewModel
import dagger.Module
import dagger.Provides

@Module
class MutualViewModelsModule {

    @Provides
    @ScreenScope
    fun provideMutualViewModel(api: Api): MutualViewModel =
            MutualViewModel(api)
}