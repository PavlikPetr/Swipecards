package com.topface.topface.di.chat

import com.topface.topface.di.Immortal
import com.topface.topface.di.scope.ScreenScope
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatViewModel
import dagger.Component


@Component(modules = arrayOf(ChatViewModelModule::class))
@ScreenScope
interface ChatViewModelComponent : Immortal {
    fun chatViewModel(): ChatViewModel
}