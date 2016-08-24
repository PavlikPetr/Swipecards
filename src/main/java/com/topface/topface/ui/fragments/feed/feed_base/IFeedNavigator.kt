package com.topface.topface.ui.fragments.feed.feed_base

import com.topface.topface.data.FeedItem

/**
 * Интерфейс навигации в  фидах
 * Created by tiberal on 12.08.16.
 */
interface IFeedNavigator {

    fun showPurchaseCoins()
    fun showPurchaseVip()
    fun <T : FeedItem> showProfile(item: T?)
    fun <T : FeedItem> showChat(item: T?)
    fun showDating()

}