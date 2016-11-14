package com.topface.topface.ui.fragments.feed.feed_base

import android.app.Activity
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.View
import com.topface.topface.data.FeedItem
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.data.search.SearchUser
import com.topface.topface.utils.IActivityDelegate
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
    fun showGiftsActivity(from: String, id: Int)
    fun showFilter()
    fun showEmptyDating()
    fun closeEmptyDating()
    fun showAdmirationPurchasePopup(currentUser: SearchUser?, transitionView: View, activity: Activity,
                                    @ColorInt fabColorResId: Int, @DrawableRes fabIconResId: Int)
}