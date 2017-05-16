package com.topface.topface.di.chat

import android.os.Bundle
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.data.FeedUser
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.chat.*
import com.topface.topface.ui.fragments.feed.enhanced.chat.adapter_components.*
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import com.topface.topface.ui.new_adapter.enhanced.ProvideItemTypeStrategyFactory.Companion.CHAT
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.delegated_properties.objectArg
import dagger.Module
import dagger.Provides

//todo если нужно делегировать вызовы активити, то в конструктор передавать IStateSaverRegistrator
@Module
class ChatModule(val chatActivity: ChatActivity, val feedUser: FeedUser?) {

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
            FeedUser::class.java -> HistoryItem.STUB_FEED_USER
            ChatLoader::class.java -> HistoryItem.STUB_CHAT_LOADER
            UserMessage::class.java -> HistoryItem.USER_MESSAGE
            FriendMessage::class.java -> HistoryItem.FRIEND_MESSAGE
            UserGift::class.java -> HistoryItem.USER_GIFT
            FriendGift::class.java -> HistoryItem.FRIEND_GIFT
            MutualStub::class.java -> HistoryItem.STUB_MUTUAL
            BuyVipStub::class.java -> HistoryItem.STUB_BUY_VIP
            else -> 0
        }
    }

    @Provides
    @FragmentScope
    fun provideCompositeAdapter(typeProvider: ITypeProvider, feedNavigator: FeedNavigator)
            = CompositeAdapter(typeProvider = typeProvider, provideItemTypeStrategyType = CHAT) {
        Bundle().apply {
            var lastId = -1
            if (it.data.isNotEmpty()) {
                val lastItem = it.data.last()
                if (lastItem is HistoryItem) {
                    lastId = lastItem.id
                }
            }
            putInt("last id", lastId)
        }
    }.apply {
        addAdapterComponent(LoaderStubComponent())
        addAdapterComponent(UserMessageComponent())
        addAdapterComponent(FriendMessageComponent(feedUser))
        addAdapterComponent(UserGiftComponent())
        addAdapterComponent(FriendGiftComponent(feedUser))
        addAdapterComponent(MutualStubComponent(feedUser))
        addAdapterComponent(BuyVipStubComponent(feedNavigator, feedUser))
    }
}
