package com.topface.topface.ui.fragments.feed.feed_base

import android.app.Activity
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.View
import com.topface.topface.data.*
import com.topface.topface.data.search.SearchUser

/**
 * Интерфейс навигации в  фидах
 * Created by tiberal on 12.08.16.
 */
interface IFeedNavigator {

    fun showPurchaseCoins(from: String, itemType: Int = -1, price: Int = -1)
    fun showPurchaseVip(from: String)
    fun <T : FeedItem> showProfile(item: T?, from: String)
    fun showProfile(item: SearchUser?, from: String)
    fun <T : FeedItem> showChat(item: T?)
    fun showChat(user: FeedUser?, answer: SendGiftAnswer?)
    fun showDating()
    fun showAddToLeader()
    fun showOwnProfile()
    fun showTakePhotoPopup()
    fun showGiftsActivity(id: Int, from: String = "")
    fun showFilter()
    fun showEmptyDating(onCancelFunction: (() -> Unit)? = null)
    fun closeEmptyDating()
    fun showAdmirationPurchasePopup(currentUser: SearchUser?, transitionView: View, activity: Activity,
                                    @ColorInt fabColorResId: Int, @DrawableRes fabIconResId: Int)

    fun showAlbum(position: Int, userId: Int, photosCount: Int, photos: Photos)
    //todo придумать свой навигатор для попапов
    fun showTrialPopup(args: Bundle)

    fun showDialogpopupMenu(item: FeedDialog)
    fun showPurchaseProduct(skuId: String, from: String)
    fun showMutualPopup(mutualUser: FeedUser)
    fun showFBInvitationPopup()
}