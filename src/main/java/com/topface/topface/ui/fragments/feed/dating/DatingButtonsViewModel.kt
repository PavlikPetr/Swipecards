package com.topface.topface.ui.fragments.feed.dating

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.topface.framework.utils.Debug
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
import com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup.AdmirationPurchasePopupViewModel
import com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup.IAnimateAdmirationPurchasePopup
import com.topface.topface.ui.fragments.feed.dating.view_etc.DatingButtonsLayout
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
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
                             private val mAnimateAdmirationPurchasePopup: IAnimateAdmirationPurchasePopup) :
        BaseViewModel<DatingButtonsLayoutBinding>(binding), DatingButtonsLayout.IDatingButtonsVisibility {

    var currentUser: SearchUser? = null
    private var mLikeSubscription: Subscription? = null
    private var mSkipSubscription: Subscription? = null
    private var mAdmirationSubscription: Subscription? = null
    val isDatingButtonsVisible = ObservableInt(View.VISIBLE)
    val isDatingButtonsLocked = ObservableBoolean(false)

    @Inject lateinit internal var mAppState: TopfaceAppState
    private val mBalanceDataSubscriptions = CompositeSubscription()
    lateinit private var mBalance: BalanceData

    private val mUpdateActionsReceiver: BroadcastReceiver

    private companion object {
        private val CURRENT_COINS_COUNT = 3
        const val CURRENT_USER = "current_user"
        const val DATING_BUTTONS_LOCKED = "dating_buttons_locked"
    }

    init {
        App.get().inject(this)
        mBalanceDataSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
            override fun onNext(balance: BalanceData?) = balance?.let {
                mBalance = it
            } ?: Unit
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

    fun showChat() = mNavigator.showChat(currentUser, null)

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

    fun sendAdmiration() {
        val isShown = App.getUserConfig().isAdmirationPurchasePopupShown
        if (!mBalance.premium && mBalance.money >= AdmirationPurchasePopupViewModel.CURRENT_COINS_COUNT && !isShown) {
            App.getUserConfig().setAdmirationPurchasePopupShown()
            mAnimateAdmirationPurchasePopup.startAnimateAdmirationPurchasePopup(binding.sendAdmiration)
        }

        if (!mBalance.premium && mBalance.money >= AdmirationPurchasePopupViewModel.CURRENT_COINS_COUNT && isShown) {
            wrapperSendAdmiration()
        }

        if (!mBalance.premium && mBalance.money < AdmirationPurchasePopupViewModel.CURRENT_COINS_COUNT) {
            mAnimateAdmirationPurchasePopup.startAnimateAdmirationPurchasePopup(binding.sendAdmiration)
        }

        if (mBalance.premium) {
            wrapperSendAdmiration()
        }
    }

    fun wrapperSendAdmiration() = sendSomething {
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
                Utils.showToastNotification(R.string.admiration_sended, 0)
                mAdmirationSubscription.safeUnsubscribe()
                mDatingButtonsView.unlockControls()
            }
        })
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
    }

    override fun onRestoreInstanceState(state: Bundle) = with(state) {
        currentUser = getParcelable<SearchUser>(CURRENT_USER)
        isDatingButtonsLocked.set(getBoolean(DATING_BUTTONS_LOCKED))
    }

    override fun onSavedInstanceState(state: Bundle) = with(state) {
        putParcelable(CURRENT_USER, currentUser)
        putBoolean(DATING_BUTTONS_LOCKED, isDatingButtonsLocked.get())
    }

    override fun release() {
        super.release()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mUpdateActionsReceiver)
        arrayOf(mLikeSubscription, mSkipSubscription, mAdmirationSubscription, mBalanceDataSubscriptions).safeUnsubscribe()
    }

    override fun showDatingButtons() = isDatingButtonsVisible.set(View.VISIBLE)

    override fun hideDatingButtons() = isDatingButtonsVisible.set(View.INVISIBLE)

}
