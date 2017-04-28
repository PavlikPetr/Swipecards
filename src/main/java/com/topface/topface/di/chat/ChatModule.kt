package com.topface.topface.di.chat

import android.os.Bundle
import com.topface.topface.data.FeedUser
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatActivity
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatIntentCreator
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatLoader
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatToolbarAvatarModel
import com.topface.topface.ui.fragments.feed.enhanced.chat.adapter_components.LoaderStubComponent
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.delegated_properties.objectArg
import dagger.Module
import dagger.Provides

//todo если нужно делегировать вызовы активити, то в конструктор передавать IStateSaverRegistrator
@Module
class ChatModule(val chatActivity: ChatActivity) {

    @Provides
    @FragmentScope
    fun provideAddPhotoHelper() = AddPhotoHelper(chatActivity)

    @Provides
    @FragmentScope
    fun provideFeedNavigator() = FeedNavigator(chatActivity as IActivityDelegate)

    @Provides
    @FragmentScope
    fun provideChatToolbarAvatarModel(navigator: FeedNavigator): ChatToolbarAvatarModel {
        val user by chatActivity.objectArg<FeedUser>(ChatIntentCreator.WHOLE_USER)
        return ChatToolbarAvatarModel(user, navigator)
    }

    @Provides
    @FragmentScope
    fun provideTypeProvider() = object : ITypeProvider {
        override fun getType(java: Class<*>) = when (java) {
            ChatLoader::class.java -> 1
            else -> 0
        }
    }

    @Provides
    @FragmentScope
    fun provideLoaderStubComponent() = LoaderStubComponent()

    @Provides
    @FragmentScope
    fun provideCompositeAdapter(typeProvider: ITypeProvider, loaderStubComponent: LoaderStubComponent)
            = CompositeAdapter(typeProvider) {
        Bundle().apply {
            //todo итем для подгрузки
        }
    }.apply {
        addAdapterComponent(loaderStubComponent)
    }
}
