package com.topface.topface.ui.fragments.feed.feed_base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import com.topface.billing.ninja.NinjaAddCardActivity
import com.topface.billing.ninja.PurchaseError
import com.topface.billing.ninja.dialogs.ErrorDialogFactory
import com.topface.billing.ninja.dialogs.IErrorDialogResultReceiver
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.data.*
import com.topface.topface.data.leftMenu.FragmentIdData
import com.topface.topface.data.leftMenu.LeftMenuSettingsData
import com.topface.topface.data.leftMenu.WrappedNavigationData
import com.topface.topface.data.search.SearchUser
import com.topface.topface.experiments.fb_invitation.FBinvitationFragment
import com.topface.topface.experiments.onboarding.question.QuestionnaireActivity
import com.topface.topface.statistics.TakePhotoStatistics
import com.topface.topface.ui.*
import com.topface.topface.ui.add_to_photo_blog.AddToPhotoBlogRedesignActivity
import com.topface.topface.ui.dialogs.new_rate.RateAppFragment
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentBoilerplateFragment
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.buy.GpPurchaseActivity
import com.topface.topface.ui.fragments.buy.PurchaseSuccessfullFragment
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.dating.DatingEmptyFragment
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupActivity
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupViewModel
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.FabTransform
import com.topface.topface.ui.fragments.dating.mutual_popup.MutualPopupFragment
import com.topface.topface.ui.fragments.feed.dialogs.DialogMenuFragment
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatIntentCreator
import com.topface.topface.ui.fragments.feed.enhanced.chat.NeedRelease
import com.topface.topface.ui.fragments.feed.enhanced.chat.chat_menu.ChatPopupMenu
import com.topface.topface.ui.fragments.feed.enhanced.chat.message_36_dialog.ChatMessage36DialogFragment
import com.topface.topface.ui.fragments.feed.photoblog.PhotoblogFragment
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity
import com.topface.topface.ui.settings.FeedbackMessageFragment
import com.topface.topface.ui.settings.SettingsContainerActivity
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetData
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.SettingsPaymentNinjaModalBottomSheet
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.Utils

/**
 * Класс для управления переходами между эркраними в фидах
 * Created by tiberal on 12.08.16.
 */
//todo раздавать через даггер 2, синглтон на фрагмент
class FeedNavigator(private val mActivityDelegate: IActivityDelegate) : IFeedNavigator {

    private val mNavigationState by lazy {
        App.getAppComponent().navigationState()
    }

    private val mEmptyDatingFragment by lazy {
        mActivityDelegate.supportFragmentManager.findFragmentByTag(DatingEmptyFragment.TAG)?.let { it as DatingEmptyFragment } ?: DatingEmptyFragment.newInstance()
    }

    override fun showPurchaseCoins(from: String, itemType: Int, price: Int) {
        mActivityDelegate.startActivity(PurchasesActivity
                .createBuyingIntent(from, itemType, price, App.get().options.topfaceOfferwallRedirect))
    }

    override fun showPurchaseVip(from: String) {
        mActivityDelegate.startActivityForResult(PurchasesActivity
                .createVipBuyIntent(null, from), PurchasesActivity.INTENT_BUY_VIP)
    }

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

    // костыльный метод, позволяющий скрыть пункт "чат" на экране профиля,
    // если он был открыт из чата версии 1
    override fun showProfileNoChat(item: FeedUser?, from: String) =
            item?.let {
                if (!it.isEmpty) {
                    mActivityDelegate.startActivity(UserProfileActivity.createIntent(null, it.photo,
                            it.id, null, false, true, Utils.getNameAndAge(it.firstName, it.age),
                            it.city.getName(), from)
                            .putExtra(UserProfileActivity.INTENT_HIDE_CHAT_IN_OVERFLOw_MENU, true))
                }
            } ?: Unit

    override fun showProfile(item: FeedUser?, from: String) =
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
    override fun <T : FeedItem> showChat(item: T?, from: String) {
        item?.let {
            it.user?.let {
                showChat(it) { ChatIntentCreator.createIntentForChatFromFeed(it, item.type, from) }
            }
        }
    }

    /**
     * Show chat from dating
     */
    override fun showChat(user: FeedUser?, answer: SendGiftAnswer?) {
        user?.let {
            showChat(user) { ChatIntentCreator.createIntentForChatFromDating(it, answer) }
        }
    }

    /**
     * Показываем чат только если, это версия с редизайном или пользователь уже VIP
     * в остальных случаях отправим его на покупку статуса
     *
     * @param user - профиль пользователя с которым необходимо показать чат
     * @param answer - объект подарка
     * @param from  - место запуска, чтобы покупка содержала plc
     */
    override fun showChatIfPossible(user: FeedUser?, answer: SendGiftAnswer?, from: String) {
        when (App.get().options.chatRedesign) {
            ChatIntentCreator.DESIGN_V1 -> user?.let {
                showChat(user) { com.topface.topface.ui.fragments.feed.enhanced.chat.ChatIntentCreator.createIntentForChatFromDating(it, answer) }
            }
            else -> if (App.get().profile.premium) {
                user?.let {
                    showChat(user) { com.topface.topface.ui.fragments.feed.enhanced.chat.ChatIntentCreator.createIntentForChatFromDating(it, answer) }
                }
            } else {
                showPurchaseVip(from)
            }
        }
    }

    private inline fun <T : FeedUser> showChat(user: T, func: T.() -> Intent?) {
        if (!user.isEmpty) {
            user.func()?.let {
                // пока не придумал кейс при котором возможна ситуация с запуском второго инстанса чата,
                // но все же пусть будет, если нет запущенного чата, то это событие обработано не будет
                App.getAppComponent().eventBus().setData(NeedRelease())
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

    override fun showTrialPopup(args: Bundle) {
        ExperimentBoilerplateFragment.newInstance(args = args)
                .show(mActivityDelegate.supportFragmentManager, ExperimentBoilerplateFragment.TAG)
    }

    override fun showMutualPopup(mutualUser: FeedUser) {
        val mMutualPopupFragment = mActivityDelegate.supportFragmentManager.findFragmentByTag(MutualPopupFragment.TAG)?.let { it as MutualPopupFragment } ?: MutualPopupFragment.getInstance(mutualUser)
        mMutualPopupFragment.show(mActivityDelegate.supportFragmentManager, MutualPopupFragment.TAG)
    }

    override fun showDialogpopupMenu(item: FeedDialog) =
            DialogMenuFragment.getInstance(item).show(mActivityDelegate.supportFragmentManager, DialogMenuFragment.TAG)

    override fun showPurchaseProduct(skuId: String, from: String) =
            mActivityDelegate.startActivityForResult(GpPurchaseActivity.getIntent(skuId, from),
                    GpPurchaseActivity.ACTIVITY_REQUEST_CODE)

    override fun showFBInvitationPopup() =
            with(mActivityDelegate.supportFragmentManager.findFragmentByTag(FBinvitationFragment.TAG)
                    ?.let { it as? FBinvitationFragment } ?: FBinvitationFragment()) {
                if (!isAdded) {
                    show(mActivityDelegate.supportFragmentManager, FBinvitationFragment.TAG)
                }
            }

    override fun showQuestionnaire(): Boolean {
        val config = App.getAppConfig()
        val startPosition = config.currentQuestionPosition
        val data = config.questionnaireData
        if (startPosition != Integer.MIN_VALUE && !data.isEmpty()) {
            mActivityDelegate.startActivityForResult(QuestionnaireActivity.getIntent(data,
                    startPosition), QuestionnaireActivity.ACTIVITY_REQUEST_CODE)
            return true
        }
        return false
    }

    override fun showRateAppFragment() {
        val mRateAppFragment = mActivityDelegate.fragmentManager.findFragmentByTag(RateAppFragment.TAG) as? RateAppFragment ?: RateAppFragment()
        if (!mRateAppFragment.isAdded) {
            mRateAppFragment.show(mActivityDelegate.fragmentManager, RateAppFragment.TAG)
        }
    }

    override fun showPurchaseSuccessfullFragment(type: String) {
        mActivityDelegate.supportFragmentManager.findFragmentByTag(PurchaseSuccessfullFragment.TAG)?.let {
            it as PurchaseSuccessfullFragment
        } ?: PurchaseSuccessfullFragment.getInstance(type).show(mActivityDelegate.supportFragmentManager, PurchaseSuccessfullFragment.TAG)
    }

    override fun showPaymentNinjaAddCardScreen(product: PaymentNinjaProduct?, source: String, isTestPurchase: Boolean, is3DSPurchase: Boolean) {
        mActivityDelegate.startActivityForResult(NinjaAddCardActivity
                .createIntent(fromInstantPurchase = false, product = product, source = source, isTestPurchase = isTestPurchase, is3DSPurchase = is3DSPurchase),
                NinjaAddCardActivity.REQUEST_CODE)
    }

    override fun showPaymentNinjaErrorDialog(singleButton: Boolean, onRetryAction: () -> Unit) {
        ErrorDialogFactory().construct(mActivityDelegate.getAlertDialogBuilder(R.style.NinjaTheme_Dialog),
                singleButton,
                object : IErrorDialogResultReceiver {
                    override fun onRetryClick() {
                        onRetryAction()
                    }

                    override fun onSwitchClick() {
                        mActivityDelegate.finish()
                    }
                }
        )
    }

    override fun showPaymentNinjaBottomSheet(data: ModalBottomSheetData) {
        mActivityDelegate.supportFragmentManager
                .findFragmentByTag(SettingsPaymentNinjaModalBottomSheet.TAG)
                ?.let { it as? SettingsPaymentNinjaModalBottomSheet } ?: SettingsPaymentNinjaModalBottomSheet.newInstance(data)
                .show(mActivityDelegate.supportFragmentManager, SettingsPaymentNinjaModalBottomSheet.TAG)
    }

    override fun showPaymentNinjaHelp() {
        mActivityDelegate.startActivityForResult(SettingsContainerActivity.getFeedbackMessageIntent(
                mActivityDelegate.applicationContext,
                FeedbackMessageFragment.FeedbackType.PAYMENT_NINJA_MESSAGE
        ), SettingsContainerActivity.INTENT_SEND_FEEDBACK)
    }

    override fun showChatPopupMenu(item: HistoryItem, itemId: Int) =
            ChatPopupMenu.newInstance(item, itemId).show(mActivityDelegate.supportFragmentManager, ChatPopupMenu.TAG)

    override fun openUrl(url: String) {
        Utils.goToUrl(mActivityDelegate, url)
    }

    override fun showPaymentNinja3DS(error: PurchaseError) {
        mActivityDelegate.startActivityForResult(NinjaAddCardActivity
                .createIntent(error),
                NinjaAddCardActivity.REQUEST_CODE)
    }

    override fun showComplainScreen(userId: Int, feedId: String?, isNeedResult: Boolean?) {
        val intent = when {
            feedId != null && isNeedResult == null -> ComplainsActivity.createIntent(userId, feedId)
            feedId == null && isNeedResult != null -> ComplainsActivity.createIntent(userId, isNeedResult)
            else -> ComplainsActivity.createIntent(userId)
        }
        mActivityDelegate.startActivityForResult(intent, ComplainsActivity.REQUEST_CODE)
    }

    override fun showUserIsTooPopularLock(user: FeedUser) =
            ChatMessage36DialogFragment.Companion.newInstance(user).show(mActivityDelegate.supportFragmentManager, ChatMessage36DialogFragment.TAG)

    override fun showBlackList() {
        mActivityDelegate.startActivity(Intent(mActivityDelegate.applicationContext, BlackListActivity::class.java))
    }
}