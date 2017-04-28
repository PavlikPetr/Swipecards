package com.topface.topface.di.chat

import android.content.Context
import com.topface.topface.api.Api
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatViewModel
import dagger.Module
import dagger.Provides


@Module
class ChatViewModelModule {

    @Provides
    @ScreenScope
    fun provideChatViewModel(context: Context, api: Api): ChatViewModel = ChatViewModel(context, api)

}