package com.topface.topface.ui.new_adapter.enhanced

import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.ui.fragments.feed.enhanced.chat.*

interface IProvideItemTypeStrategy {
    val typeProvider: ITypeProvider
    fun provide(item: Any): Int
}

class DefaultProvideItemTypeStrategy(override val typeProvider: ITypeProvider): IProvideItemTypeStrategy {
    override fun provide(item: Any) = typeProvider.getType(item.javaClass)
}

class ChatProvideItemTypeStrategy(override val typeProvider: ITypeProvider): IProvideItemTypeStrategy {
    override fun provide(item: Any): Int {
        if (item is IChatItem) {
            when(item.getItemType()) {
                HistoryItem.USER_GIFT -> return typeProvider.getType(UserGift::class.java)
                HistoryItem.USER_MESSAGE -> return typeProvider.getType(UserMessage::class.java)
                HistoryItem.FRIEND_GIFT -> return typeProvider.getType(FriendGift::class.java)
                HistoryItem.FRIEND_MESSAGE -> return typeProvider.getType(FriendMessage::class.java)
            }
        }
        return typeProvider.getType(item.javaClass)
    }
}

class DummyStrategy(override val typeProvider: ITypeProvider): IProvideItemTypeStrategy {
    override fun provide(item: Any) = 0
}

class ProvideItemTypeStrategyFactory(val typeProvider: ITypeProvider) {
    companion object {
        const val DEFAULT = 1
        const val CHAT = 2
    }
    fun construct(type: Int) = when(type) {
        DEFAULT -> DefaultProvideItemTypeStrategy(typeProvider)
        CHAT -> ChatProvideItemTypeStrategy(typeProvider)
        else -> DummyStrategy(typeProvider)
    }
}