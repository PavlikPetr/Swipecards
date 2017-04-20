package com.topface.topface.di.chat

import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatViewModel
import dagger.Module
import dagger.Provides


@Module
class ChatViewModelModule {

    @Provides
    @ScreenScope
    fun provideFansViewModel(): ChatViewModel = ChatViewModel()

}