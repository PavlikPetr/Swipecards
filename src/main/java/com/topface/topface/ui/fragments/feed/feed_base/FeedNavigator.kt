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
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Photos
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.data.leftMenu.FragmentIdData
import com.topface.topface.data.leftMenu.LeftMenuSettingsData
import com.topface.topface.data.leftMenu.NavigationState
import com.topface.topface.data.leftMenu.WrappedNavigationData
import com.topface.topface.data.search.SearchUser
import com.topface.topface.statistics.TakePhotoStatistics
import com.topface.topface.ui.*
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentBoilerplateFragment
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupActivity
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupViewModel
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.FabTransform
import com.topface.topface.ui.fragments.feed.dating.DatingEmptyFragment
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
        DatingEmptyFragment.newInstance()
    }

    init {
        App.get().inject(this)
    }

    override fun showPurchaseCoins() = mActivityDelegate.startActivity(PurchasesActivity
            .createBuyingIntent("EmptyLikes", App.get().options.topfaceOfferwallRedirect))

    override fun showPurchaseVip() = mActivityDelegate.startActivityForResult(PurchasesActivity
            .createVipBuyIntent(null, "Likes"), PurchasesActivity.INTENT_BUY_VIP)

    override fun <T : FeedItem> showProfile(item: T?) {
        item?.let {
            if (!it.user.isEmpty) {
                val user = it.user
                mActivityDelegate.startActivity(UserProfileActivity.createIntent(null, user.photo,
                        user.id, it.id, false, true, Utils.getNameAndAge(user.firstName, user.age),
                        user.city.getName()))
            }
        }
    }

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
    override fun showChat(user: SearchUser?, answer: SendGiftAnswer?) {
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
            AddToPhotoBlogActivity::class.java), PhotoblogFragment.ADD_TO_PHOTO_BLOG_ACTIVITY_ID)

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

    override fun showGiftsActivity(id: Int) {
        mActivityDelegate.startActivityForResult(
                GiftsActivity.getSendGiftIntent(mActivityDelegate.applicationContext, id, false),
                GiftsActivity.INTENT_REQUEST_GIFT
        )
    }

    override fun showEmptyDating() = mEmptyDatingFragment.show(mActivityDelegate.supportFragmentManager, "DATING_EMPTY_FRAGMENT")

    override fun closeEmptyDating() {
        mEmptyDatingFragment.dialog?.cancel()
    }

    override fun showFilter() = mActivityDelegate.startActivityForResult(Intent(mActivityDelegate.applicationContext,
            EditContainerActivity::class.java), EditContainerActivity.INTENT_EDIT_FILTER)

    override fun showAlbum(position: Int, userId: Int, photosCount: Int, photos: Photos) =
            mActivityDelegate.startActivityForResult(PhotoSwitcherActivity.getPhotoSwitcherIntent(position, userId, photosCount, photos),
                    PhotoSwitcherActivity.PHOTO_SWITCHER_ACTIVITY_REQUEST_CODE)

    override fun showTrialPopup(type: Long, args: Bundle) {
        ExperimentBoilerplateFragment.newInstance(type, args = args)
                .show(mActivityDelegate., ExperimentBoilerplateFragment.TAG)
    }

}
