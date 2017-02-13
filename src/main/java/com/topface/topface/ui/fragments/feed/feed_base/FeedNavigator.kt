package com.topface.topface.ui.fragments.feed.feed_base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import com.topface.topface.App
import com.topface.topface.data.*
import com.topface.topface.data.leftMenu.FragmentIdData
import com.topface.topface.data.leftMenu.LeftMenuSettingsData
import com.topface.topface.data.leftMenu.NavigationState
import com.topface.topface.data.leftMenu.WrappedNavigationData
import com.topface.topface.data.search.SearchUser
import com.topface.topface.statistics.TakePhotoStatistics
import com.topface.topface.ui.*
import com.topface.topface.ui.add_to_photo_blog.AddToPhotoBlogRedesignActivity
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentBoilerplateFragment
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.buy.GpPurchaseActivity
import com.topface.topface.ui.fragments.dating.DatingEmptyFragment
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupActivity
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupViewModel
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.FabTransform
import com.topface.topface.ui.fragments.feed.dialogs.DialogMenuFragment
import com.topface.topface.ui.fragments.feed.photoblog.PhotoblogFragment
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.Utils
import javax.inject.Inject

/**
 * Класс для управления переходами между эркраними в фидах
 * Created by tiberal on 12.08.16.
 */
//todo раздавать через даггер 2, синглтон на фрагмент
class FeedNavigator(private val mActivityDelegate: IActivityDelegate) : IFeedNavigator {

    @Inject lateinit var mNavigationState: NavigationState

    private val mEmptyDatingFragment by lazy {
        mActivityDelegate.supportFragmentManager.findFragmentByTag(DatingEmptyFragment.TAG)?.let { it as DatingEmptyFragment } ?: DatingEmptyFragment.newInstance()
    }

    init {
        App.get().inject(this)
    }

    override fun showPurchaseCoins(from: String, itemType: Int, price: Int) = mActivityDelegate.startActivity(PurchasesActivity
            .createBuyingIntent(from, itemType, price, App.get().options.topfaceOfferwallRedirect))

    override fun showPurchaseVip(from: String) = mActivityDelegate.startActivityForResult(PurchasesActivity
            .createVipBuyIntent(null, from), PurchasesActivity.INTENT_BUY_VIP)

    override fun <T : FeedItem> showProfile(item: T?, from: String) {
        item?.let {
            if (!it.user.isEmpty) {
                val user = it.user
                mActivityDelegate.startActivity(UserProfileActivity.createIntent(null, user.photo,
                        user.id, it.id, false, true, Utils.getNameAndAge(user.firstName, user.age),
                        user.city.getName(), from))
            }
        }
    }

    override fun showProfile(item: SearchUser?, from: String) =
            item?.let {
                if (!it.isEmpty) {
                    mActivityDelegate.startActivity(UserProfileActivity.createIntent(null, it.photo,
                            it.id, null, false, true, Utils.getNameAndAge(it.firstName, it.age),
                            it.city.getName(), from))
                }
            } ?: Unit

    /**
     * Show chat from feed
     */
    override fun <T : FeedItem> showChat(item: T?) {
        item?.let {
            it.user?.let {
                showChat(it) { ChatActivity.createIntent(id, sex, nameAndAge, city.name, null, photo, false, item.type, banned) }
            }
        }
    }

    /**
     * Show chat from dating
     */
    override fun showChat(user: FeedUser?, answer: SendGiftAnswer?) {
        user?.let {
            showChat(user) { ChatActivity.createIntent(id, sex, nameAndAge, city.name, null, photo, false, answer, banned) }
        }
    }

    private inline fun <T : FeedUser> showChat(user: T, func: T.() -> Intent?) {
        if (!user.isEmpty) {
            user.func()?.let {
                mActivityDelegate.startActivityForResult(it, ChatActivity.REQUEST_CHAT)
            }
        }
    }

    override fun showDating() = mNavigationState
            .emmitNavigationState(WrappedNavigationData(LeftMenuSettingsData(FragmentIdData.DATING),
                    WrappedNavigationData.SELECT_EXTERNALY))

    override fun showAddToLeader() = mActivityDelegate.startActivityForResult(Intent(mActivityDelegate.applicationContext,
            if (App.get().options.peopleNearbyRedesignEnabled) AddToPhotoBlogRedesignActivity::class.java else AddToPhotoBlogActivity::class.java), PhotoblogFragment.ADD_TO_PHOTO_BLOG_ACTIVITY_ID)

    override fun showOwnProfile() = mActivityDelegate.startActivity(Intent(mActivityDelegate.applicationContext, OwnProfileActivity::class.java))

    override fun showTakePhotoPopup() = TakePhotoPopup.newInstance(TakePhotoStatistics.PLC_ADD_TO_LEADER)
            .show(mActivityDelegate.supportFragmentManager, TakePhotoPopup.TAG)

    @SuppressLint("NewApi")
    override fun showAdmirationPurchasePopup(currentUser: SearchUser?, transitionView: View, activity: Activity, @ColorInt fabColorResId: Int, @DrawableRes fabIconResId: Int) {
        val intent = Intent(activity, AdmirationPurchasePopupActivity::class.java)
        intent.putExtra(AdmirationPurchasePopupActivity.CURRENT_USER, currentUser)
        if (Utils.isLollipop()) {
            FabTransform.addExtras(intent, fabColorResId, fabIconResId)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView,
                    AdmirationPurchasePopupViewModel.TRANSITION_NAME)
            activity.startActivityForResult(intent, AdmirationPurchasePopupActivity.INTENT_ADMIRATION_PURCHASE_POPUP, options.toBundle())
        } else {
            activity.startActivityForResult(intent, AdmirationPurchasePopupActivity.INTENT_ADMIRATION_PURCHASE_POPUP)
        }
    }

    override fun showGiftsActivity(id: Int, from: String) {
        mActivityDelegate.startActivityForResult(
                GiftsActivity.getSendGiftIntent(mActivityDelegate.applicationContext, id, false, from),
                GiftsActivity.INTENT_REQUEST_GIFT
        )
    }

    override fun showEmptyDating(onCancelFunction: (() -> Unit)?) = with(mEmptyDatingFragment) {
        if (onCancelFunction != null) {
            setOnCancelListener { onCancelFunction() }
        }
        show(mActivityDelegate.supportFragmentManager, DatingEmptyFragment.TAG)
    }

    override fun closeEmptyDating() {
        mEmptyDatingFragment.setOnCancelListener(null)
        mEmptyDatingFragment.dialog?.cancel()
    }

    override fun showFilter() = mActivityDelegate.startActivityForResult(Intent(mActivityDelegate.applicationContext,
            EditContainerActivity::class.java), EditContainerActivity.INTENT_EDIT_FILTER)

    override fun showAlbum(position: Int, userId: Int, photosCount: Int, photos: Photos) =
            mActivityDelegate.startActivityForResult(PhotoSwitcherActivity.getPhotoSwitcherIntent(position, userId, photosCount, photos),
                    PhotoSwitcherActivity.PHOTO_SWITCHER_ACTIVITY_REQUEST_CODE)

    override fun showTrialPopup(type: Long, args: Bundle) {
        ExperimentBoilerplateFragment.newInstance(type, args = args)
                .show(mActivityDelegate.supportFragmentManager, ExperimentBoilerplateFragment.TAG)
    }

    override fun showDialogpopupMenu(item: FeedDialog) =
            DialogMenuFragment.getInstance(item).show(mActivityDelegate.supportFragmentManager, DialogMenuFragment.TAG)

    override fun showPurchaseProduct(skuId: String, from: String) =
            mActivityDelegate.startActivityForResult(GpPurchaseActivity.getIntent(skuId, from),
                    GpPurchaseActivity.ACTIVITY_REQUEST_CODE)

}