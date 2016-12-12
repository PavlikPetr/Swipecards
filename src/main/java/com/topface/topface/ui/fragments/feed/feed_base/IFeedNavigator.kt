package com.topface.topface.ui.fragments.feed.feed_base

import android.app.Activity
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.View
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Photos
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.data.search.SearchUser

/**
 * Интерфейс навигации в  фидах
 * Created by tiberal on 12.08.16.
 */
interface IFeedNavigator {

    fun showPurchaseCoins()
    fun showPurchaseVip()
    fun <T : FeedItem> showProfile(item: T?)
    fun <T : FeedItem> showChat(item: T?)
    fun showChat(user: FeedUser?, answer: SendGiftAnswer?)
    fun showDating()
    fun showAddToLeader()
    fun showOwnProfile()
    fun showTakePhotoPopup()
    fun showGiftsActivity(id: Int)
    fun showFilter()
    fun showEmptyDating(onCancelFunction: (() -> Unit)? = null)
    fun closeEmptyDating()
    fun showAdmirationPurchasePopup(currentUser: SearchUser?, transitionView: View, activity: Activity,
                                    @ColorInt fabColorResId: Int, @DrawableRes fabIconResId: Int)

    fun showAlbum(position: Int, userId: Int, photosCount: Int, photos: Photos)
    //todo придумать свой навигатор для попапов
    fun showTrialPopup(type: Long, args: Bundle)
}