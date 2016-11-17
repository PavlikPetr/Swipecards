package com.topface.topface.ui.fragments.dating

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
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.RetryRequestReceiver
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Rate
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.DatingButtonsLayoutBinding
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.SendLikeRequest
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.AdmirationPurchasePopupActivity
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.IStartAdmirationPurchasePopup
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.toolbar.IAppBarState
import com.topface.topface.utils.EasyTracker
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.cache.SearchCacheManager
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscriber
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * VM for dating buttons
 * Created by tiberal on 11.10.16.
 */
class DatingButtonsViewModel(binding: DatingButtonsLayoutBinding,
                             private val mApi: FeedApi,
                             private val mNavigator: IFeedNavigator,
                             private val mUserSearchList: CachableSearchList<SearchUser>,
                             private val mDatingButtonsEvents: DatingButtonsEventsDelegate,
                             private val mDatingButtonsView: IDatingButtonsView,
                             private val mEmptySearchVisibility: IEmptySearchVisibility,
                             private val mStartAdmirationPurchasePopup: IStartAdmirationPurchasePopup) :
        BaseViewModel<DatingButtonsLayoutBinding>(binding), IAppBarState {

    var currentUser: SearchUser? = null
    private var mLikeSubscription: Subscription? = null
    private var mSkipSubscription: Subscription? = null
    private var mAdmirationSubscription: Subscription? = null
    val isDatingButtonsVisible = ObservableInt(View.VISIBLE)
    val isDatingButtonsLocked = ObservableBoolean(false)

    @Inject lateinit internal var mAppState: TopfaceAppState
    private val mBalanceDataSubscriptions = CompositeSubscription()
    private var mBalance: BalanceData? = null

    private val mUpdateActionsReceiver: BroadcastReceiver

    private companion object {
        const val CURRENT_USER = "current_user_dating_buttons"
        const val DATING_BUTTONS_LOCKED = "dating_buttons_locked"
        const val DATING_BUTTON_VISIBILITY = "dating_button_visibility"
    }

    init {
        App.get().inject(this)
        mBalanceDataSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
            override fun onNext(balance: BalanceData?) = balance.let {
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

    fun showChat() = if (App.get().profile.premium) {
        mNavigator.showChat(currentUser, null)
    } else {
        mNavigator.showPurchaseVip()
    }

    fun skip() = currentUser?.let {
        if (!it.skipped && !it.rated) {
            if (App.isOnline()) {
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
                if (mUserSearchList.isCurrentUserLast) {
                    //todo чтос делать с диалогм, если запрос не дошел
                    // showRetryDialog()
                }
            }
        }
    }

    fun sendLike() = sendSomething {
        if (!it.rated) {
            mLikeSubscription = mApi.callSendLike(it.id, App.get().options.blockUnconfirmed,
                    getMutualId(it), SendLikeRequest.FROM_SEARCH).subscribe(object : Subscriber<Rate>() {
                override fun onCompleted() = RxUtils.safeUnsubscribe(mLikeSubscription)
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
        }/* else {
            showNextUser()
        }*/
    }

    fun validateSendAdmiration() {
        val priceAdmiration = App.get().options.priceAdmiration
        val isShown = App.getUserConfig().isAdmirationPurchasePopupShown

        mBalance?.let {
            val hasMoneyForAdmiration = it.money >= priceAdmiration
            if (it.premium || hasMoneyForAdmiration && isShown) {
                sendAdmiration()
            } else {
                startAdmirationPurchasePopup()
            }
        }
    }

    private fun startAdmirationPurchasePopup() {
        App.getUserConfig().setAdmirationPurchasePopupShown()
        mStartAdmirationPurchasePopup.startAnimateAdmirationPurchasePopup(binding.sendAdmiration,
                R.color.dating_fab_small, R.drawable.admiration)
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK &&
                requestCode == AdmirationPurchasePopupActivity.INTENT_ADMIRATION_PURCHASE_POPUP) {
            sendAdmiration()
        }
    }

    private inline fun sendSomething(func: (SearchUser) -> Unit) =
            if (!isNeedTakePhoto()) {
                currentUser?.let {
                    showNextUser()
                    func(it)
                }
            } else {
                mDatingButtonsEvents.showTakePhoto()
            }

    private fun getMutualId(user: SearchUser) = if (user.isMutualPossible)
        SendLikeRequest.DEFAULT_MUTUAL
    else
        SendLikeRequest.DEFAULT_NO_MUTUAL

    private fun showNextUser() = mUserSearchList.nextUser()?.let {
        mEmptySearchVisibility.hideEmptySearchDialog()
        mDatingButtonsView.unlockControls()
        mDatingButtonsEvents.onNewSearchUser(it)
        currentUser = it
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
