package com.topface.topface.ui.fragments.feed.feed_base

import android.app.Activity
import android.view.View
import com.topface.topface.data.FeedItem
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.data.search.SearchUser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi

/**
 * Интерфейс навигации в  фидах
 * Created by tiberal on 12.08.16.
 */
interface IFeedNavigator {

    fun showPurchaseCoins()
    fun showPurchaseVip()
    fun <T : FeedItem> showProfile(item: T?)
    fun <T : FeedItem> showChat(item: T?)
    fun showChat(user: SearchUser?, answer: SendGiftAnswer?)
    fun showDating()
    fun showAddToLeader()
    fun showOwnProfile()
    fun showTakePhotoPopup()
    fun showAdmirationPurchasePopup(currentUser: SearchUser?, transitionView: View, activity: Activity)
    fun showEmptyDating()
    fun showFilter()

}