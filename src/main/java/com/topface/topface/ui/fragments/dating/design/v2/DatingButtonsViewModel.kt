package com.topface.topface.ui.fragments.dating.design.v2

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.RetryRequestReceiver
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Rate
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.DatingButtonsLayoutV2Binding
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.SendLikeRequest
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.statistics.AuthStatistics
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.dating.DatingButtonsEventsDelegate
import com.topface.topface.ui.fragments.dating.IDatingButtonsView
import com.topface.topface.ui.fragments.dating.IEmptySearchVisibility
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupActivity
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.IStartAdmirationPurchasePopup
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.toolbar.IAppBarState
import com.topface.topface.utils.EasyTracker
import com.topface.topface.utils.Utils
import com.topface.topface.utils.cache.SearchCacheManager
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscriber
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * VM for dating buttons (design version 2)
 * Created by tiberal on 11.10.16. (copypasted by m.bayutin)
 */
class DatingButtonsViewModel(binding: DatingButtonsLayoutV2Binding,
                             private val mApi: FeedApi,
                             private val mNavigator: IFeedNavigator,
                             private val mUserSearchList: CachableSearchList<SearchUser>,
                             private val mDatingButtonsEvents: DatingButtonsEventsDelegate,
                             private val mDatingButtonsView: IDatingButtonsView,
                             private val mEmptySearchVisibility: IEmptySearchVisibility,
                             private val mStartAdmirationPurchasePopup: IStartAdmirationPurchasePopup) :
        BaseViewModel<DatingButtonsLayoutV2Binding>(binding), IAppBarState {

    var currentUser: SearchUser? = null
        set(value) {
            Debug.log("LOADER_INTEGRATION user setter")
            if (mLockDatingButtonsVisibility) {
                mLockDatingButtonsVisibility = false
                isDatingProgressBarVisible.set(View.GONE)
                isDatingButtonsVisible.set(View.VISIBLE)
            }
            field = value
        }
    private var mLikeSubscription: Subscription? = null
    private var mSkipSubscription: Subscription? = null
    private var mAdmirationSubscription: Subscription? = null
    val isDatingButtonsVisible = object : ObservableInt(View.INVISIBLE) {
        override fun set(value: Int) {
            if (!mLockDatingButtonsVisibility) {
                super.set(value)
            }
        }
    }
    val isDatingProgressBarVisible = ObservableInt(View.VISIBLE)
    val isDatingButtonsLocked = ObservableBoolean(false)

    /*
       isDatingButtonsVisible  неистово срет вызовами, по этому не можем ипользовать эту переменную
       для определения состояния кнопок. И поэтому пояаилось это.
     */
    private var mLockDatingButtonsVisibility = true

    private val mAppState by lazy {
        App.getAppComponent().appState()
    }
    private val mBalanceDataSubscriptions = CompositeSubscription()
    private var mBalance: BalanceData? = null
    private val mIsMutualPopupEnabled = App.get().options.mutualPopupEnabled
    private val mUpdateActionsReceiver: BroadcastReceiver

    companion object {
        private const val CURRENT_USER = "current_user_dating_buttons"
        private const val DATING_BUTTONS_LOCKED = "dating_buttons_locked"
        private const val DATING_BUTTON_VISIBILITY = "dating_button_visibility"
        private const val MAX_LIKE_AMOUNT = 4
        const val BUTTON_FROM_ANCHOR = 0
        const val BUTTON_FROM_DATING_FRAGMENT = 1
    }

    init {
        mBalanceDataSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(shortSubscription {
            it?.let {
                mBalance = it
            }
        }))

        mUpdateActionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val type = intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE) as BlackListAndBookmarkHandler.ActionTypes?
                if (type != null) {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (type) {
                        BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST -> {
                            skip()
                        }
                        BlackListAndBookmarkHandler.ActionTypes.SYMPATHY -> {
                            binding.sendLike.isEnabled = false
                            binding.sendAdmiration.isEnabled = false
                            currentUser?.let {
                                it.rated = true
                            }
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(mUpdateActionsReceiver, IntentFilter(RetryRequestReceiver.RETRY_INTENT))

    }

    private fun isNeedTakePhoto() = !App.getConfig().userConfig.isUserAvatarAvailable
            && App.get().profile.photo == null

    fun showChat() = mNavigator.showChat(currentUser, null, "Dating")

    fun skip() = currentUser?.let {
        if (!it.skipped && !it.rated) {
            showNextUser()
            mSkipSubscription = mApi.callSkipRequest(it.id).subscribe(object : Subscriber<IApiResponse>() {
                override fun onCompleted() = mSkipSubscription.safeUnsubscribe()
                override fun onError(e: Throwable?) = e?.printStackTrace() ?: Unit
                override fun onNext(t: IApiResponse?) {
                    for (user in mUserSearchList) {
                        if (user.id == it.id) {
                            user.skipped = true
                            return
                        }
                    }
                    mDatingButtonsView.unlockControls()
                }
            })
        } else {
            showNextUser()
        }
    }

    fun sendLike() = sendSomething {
        if (!it.rated) {
            mLikeSubscription = mApi.callSendLike(it.id, App.get().options.blockUnconfirmed,
                    getMutualId(it), SendLikeRequest.FROM_SEARCH)
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : Subscriber<Rate>() {
                        override fun onCompleted() {
                            mLikeSubscription.safeUnsubscribe()
                            validateDeviceActivation()
                            if (it.isMutualPossible && mIsMutualPopupEnabled) {
                                mNavigator.showMutualPopup(it)
                            }
                        }

                        override fun onError(e: Throwable?) {
                            it.rated = false
                            mDatingButtonsView.unlockControls()
                        }

                        override fun onNext(rate: Rate?) {
                            it.rated = true
                            SearchCacheManager.markUserAsRatedInCache(it.id)
                            mDatingButtonsView.unlockControls()
                        }
                    })
        } else {
            showNextUser()
        }
    }

    private fun validateDeviceActivation() {
        val appConfig = App.getAppConfig()
        var counter = appConfig.deviceActivationCounter
        if (counter < MAX_LIKE_AMOUNT) {
            appConfig.deviceActivationCounter = ++counter
        } else {
            if (!appConfig.isDeviceActivated) {
                AuthStatistics.sendDeviceActivated()
                appConfig.setDeviceActivated()
            }
        }
        appConfig.saveConfig()
    }

    fun validateSendAdmiration(viewID: Int) {
        val priceAdmiration = App.get().options.priceAdmiration
        val isShown = App.getUserConfig().isAdmirationPurchasePopupShown

        mBalance?.let {
            val hasMoneyForAdmiration = it.money >= priceAdmiration
            if (it.premium || hasMoneyForAdmiration && isShown) {
                sendAdmiration()
            } else {
                startAdmirationPurchasePopup(viewID)
            }
        }
    }

    private fun startAdmirationPurchasePopup(viewID: Int) {
        App.getUserConfig().setAdmirationPurchasePopupShown()
        mStartAdmirationPurchasePopup.startAnimateAdmirationPurchasePopup(viewID,
                R.color.black, R.drawable.admiration)
    }

    fun sendAdmiration() = sendSomething {
        val mutualId = getMutualId(it)
        mDatingButtonsView.lockControls()
        mAdmirationSubscription = mApi.callSendAdmiration(it.id, App.get().options.blockUnconfirmed,
                mutualId, SendLikeRequest.FROM_SEARCH).subscribe(object : Subscriber<Rate>() {
            override fun onError(e: Throwable?) {
                mDatingButtonsView.unlockControls()
                e?.printStackTrace()
            }

            override fun onNext(rate: Rate?) {
                EasyTracker.sendEvent("Dating", "Rate",
                        "AdmirationSend" + if (mutualId == SendLikeRequest.DEFAULT_MUTUAL) "mutual" else Utils.EMPTY,
                        App.get().options.priceAdmiration.toLong())
            }

            override fun onCompleted() {
                Utils.showToastNotification(R.string.admiration_sended, Toast.LENGTH_SHORT)
                mAdmirationSubscription.safeUnsubscribe()
                mDatingButtonsView.unlockControls()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK &&
                requestCode == AdmirationPurchasePopupActivity.INTENT_ADMIRATION_PURCHASE_POPUP) {
            sendAdmiration()
        }
        if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            showProgress()
        }
    }

    private fun sendSomething(func: (SearchUser) -> Unit) {
        if (App.isOnline()) {
            if (!isNeedTakePhoto()) {
                currentUser?.let {
                    showNextUser()
                    func(it)
                }
            } else {
                mDatingButtonsEvents.showTakePhoto()
            }
        }
    }

    private fun getMutualId(user: SearchUser) = if (user.isMutualPossible)
        SendLikeRequest.DEFAULT_MUTUAL
    else
        SendLikeRequest.DEFAULT_NO_MUTUAL

    private fun showNextUser() {
        if (mUserSearchList.searchPosition == mUserSearchList.size - 1 && mUserSearchList.isNeedPreload) {
            showProgress()
            return
        } else {
            hideProgress()
        }
        mUserSearchList.nextUser()?.let {
            mEmptySearchVisibility.hideEmptySearchDialog()
            mDatingButtonsView.unlockControls()
            mDatingButtonsEvents.onNewSearchUser(it)
            currentUser = it
        }
    }

    private fun showProgress() {
        mDatingButtonsEvents.onShowProgress()
        isDatingProgressBarVisible.set(View.VISIBLE)
        isDatingButtonsVisible.set(View.INVISIBLE)
        mLockDatingButtonsVisibility = true
    }

    private fun hideProgress() {
        mDatingButtonsEvents.onHideProgress()
        mLockDatingButtonsVisibility = false
        isDatingProgressBarVisible.set(View.GONE)
        isDatingButtonsVisible.set(View.VISIBLE)
    }

    override fun onRestoreInstanceState(state: Bundle) = with(state) {
        currentUser = getParcelable<SearchUser>(CURRENT_USER)
        isDatingButtonsLocked.set(getBoolean(DATING_BUTTONS_LOCKED))
        isDatingButtonsVisible.set(getInt(DATING_BUTTON_VISIBILITY))
    }

    override fun onSavedInstanceState(state: Bundle) = with(state) {
        putParcelable(CURRENT_USER, currentUser)
        putBoolean(DATING_BUTTONS_LOCKED, isDatingButtonsLocked.get())
        putInt(DATING_BUTTON_VISIBILITY, isDatingButtonsVisible.get())
    }

    override fun release() {
        super.release()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mUpdateActionsReceiver)
        arrayOf(mLikeSubscription, mSkipSubscription, mAdmirationSubscription, mBalanceDataSubscriptions).safeUnsubscribe()
    }

    fun showDatingButtons() = isDatingButtonsVisible.set(View.VISIBLE)

    fun hideDatingButtons() = isDatingButtonsVisible.set(View.INVISIBLE)

    override fun isScrimVisible(isVisible: Boolean) =
            if (isVisible) hideDatingButtons() else showDatingButtons()


    override fun isCollapsed(isCollapsed: Boolean) =
            if (isCollapsed) {
                hideDatingButtons()
            } else Unit
}
