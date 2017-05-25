package com.topface.topface.di.chat

import android.content.Context
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
    fun provideChatViewModel(context: Context, api: Api, eventBus: EventBus, state: TopfaceAppState): ChatViewModel = ChatViewModel(context, api, eventBus, state)

}