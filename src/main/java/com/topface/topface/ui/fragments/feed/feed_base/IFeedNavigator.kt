package com.topface.topface.ui.fragments.feed.feed_base

import android.app.Activity
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.View
import com.topface.billing.ninja.PurchaseError
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.data.*
import com.topface.topface.data.search.SearchUser
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetData

/**
 * Интерфейс навигации в  фидах
 * Created by tiberal on 12.08.16.
 */
interface IFeedNavigator {

    fun showPurchaseCoins(from: String, itemType: Int = -1, price: Int = -1)
    fun showPurchaseVip(from: String)
    fun <T : FeedItem> showProfile(item: T?, from: String)
    fun showProfile(item: FeedUser?, from: String)
    // костыльный метод
    fun showProfileNoChat(item: FeedUser?, from: String)
    fun <T : FeedItem> showChat(item: T?,from: String)
    fun showChat(user: FeedUser?, answer: SendGiftAnswer?)
    fun showChatIfPossible(user: FeedUser?, answer: SendGiftAnswer?, from: String)
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
    fun showPurchaseSuccessfullFragment(type: String)
    fun showMutualPopup(mutualUser: FeedUser)
    fun showPaymentNinjaAddCardScreen(product: PaymentNinjaProduct? = null, source: String, isTestPurchase: Boolean = false, is3DSPurchase: Boolean = false)
    fun showPaymentNinjaBottomSheet(data: ModalBottomSheetData)
    fun showPaymentNinjaErrorDialog(singleButton: Boolean, onRetryAction: () -> Unit)
    fun showPaymentNinjaHelp()
    fun showFBInvitationPopup()
    fun showQuestionnaire(): Boolean
    fun showRateAppFragment()
    fun showPaymentNinja3DS(error: PurchaseError)

    fun openUrl(url: String)
    fun showChatPopupMenu(item: HistoryItem, position: Int)
    fun showComplainScreen(userId: Int, feedId: String? = null, isNeedResult: Boolean? = null)
    fun showUserIsTooPopularLock(user: FeedUser)
    fun showBlackList()
}