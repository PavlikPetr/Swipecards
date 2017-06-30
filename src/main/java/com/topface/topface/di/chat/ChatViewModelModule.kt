package com.topface.topface.di.chat

import com.topface.topface.api.Api
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatViewModel
import dagger.Module
import dagger.Provides


@Module
class ChatViewModelModule {

    @Provides
    @ScreenScope
    fun provideChatViewModel(api: Api, eventBus: EventBus, state: TopfaceAppState): ChatViewModel = ChatViewModel(api, eventBus, state)

}