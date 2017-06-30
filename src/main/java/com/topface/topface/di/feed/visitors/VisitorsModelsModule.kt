package com.topface.topface.di.feed.visitors

import com.topface.topface.api.Api
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorsViewModel
import dagger.Module
import dagger.Provides

@Module
class VisitorsModelsModule {

    @Provides
    @ScreenScope
    fun provideVisitorsViewModel(api: Api): VisitorsViewModel =
            VisitorsViewModel(api)

}
