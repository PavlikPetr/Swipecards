package com.topface.topface.di.feed.fans

import com.topface.topface.api.Api
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansViewModel
import dagger.Module
import dagger.Provides

@Module
class FansViewModelsModule {

    @Provides
    @ScreenScope
    fun provideFansViewModel(api: Api): FansViewModel =
            FansViewModel(api)

}