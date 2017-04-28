package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.content.Context
import android.content.IntentFilter
import android.databinding.ObservableInt
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.api.Api
import com.topface.topface.api.responses.History
import com.topface.topface.data.FeedUser
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.ChatData
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.RxObservableField
import com.topface.topface.utils.rx.observeBroabcast
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Observer
import rx.Subscription
import java.util.concurrent.TimeUnit

class ChatViewModel(private val mContext: Context, private val mApi: Api) : BaseViewModel() {

    companion object {
        private const val DEFAULT_CHAT_UPDATE_PERIOD = 5000//30
    }

    internal var navigator: FeedNavigator? = null

    val isComplainVisibile = ObservableInt(View.VISIBLE)
    val isChatVisible = ObservableInt(View.VISIBLE)
    val message = RxObservableField<String>()
    val chatData = ChatData()
    private val pullToRefreshSubscription: Subscription? = null
    private var mUpdateHistorySubscription: Subscription? = null
    private var mDialogGetSubscription: Subscription? = null

    private val mNewMessageBroabcastSubscription: Subscription? = null
    private val mVipBoughtBroabcastSubscription: Subscription? = null
    private var mUser: FeedUser? = null

    override fun bind() {
        mUser = args?.getParcelable(ChatIntentCreator.WHOLE_USER)
        mUpdateHistorySubscription = Observable.merge(
                createGCMUpdateObservable(),
                createTimerUpdateObservable(),
                createP2RObservable()).subscribe(shortSubscription {
            // update(it)
            Debug.log("FUCKING_CHAT some update from merge $it")
        })
    }

    private fun createGCMUpdateObservable() =
            mContext.observeBroabcast(IntentFilter(GCMUtils.GCM_NOTIFICATION))
                    .map {
                        val id = try {
                            Integer.parseInt(it.getStringExtra(GCMUtils.USER_ID_EXTRA))
                        } catch (e: NumberFormatException) {
                            -1
                        }
                        val type = it.getIntExtra(GCMUtils.GCM_TYPE, -1)
                        Pair(id, type)
                    }
                    .filter {
                        it.first != -1 && mUser?.id == it.first
                    }
                    .map {
                        GCMUtils.cancelNotification(mContext, it.second)
                        it.first
                    }

    private fun createTimerUpdateObservable() = Observable.
            interval(0, DEFAULT_CHAT_UPDATE_PERIOD.toLong(), TimeUnit.MILLISECONDS)
            .map {
                val user = mUser
                if (user != null) {
                    user.id
                } else {
                    Debug.log("CHAT incorrect user id")
                    -1
                }
            }

    //todo заменить при имплементацию птр
    private fun createP2RObservable() = Observable.just(-1)

    private var mIsNeedShowAddPhoto = true

    private fun takePhotoIfNeed() {
        if (mIsNeedShowAddPhoto) {
            mIsNeedShowAddPhoto = false
            if (isTakePhotoApplicable()) {
                navigator?.showTakePhotoPopup()
            }
        }
    }

    private fun isTakePhotoApplicable() = !App.getConfig().userConfig.isUserAvatarAvailable &&
            App.get().profile.photo == null


    /*
    событие на обновление агрегировать с эмитов гцм, птр, таймера
     */
    private fun update(userId: Int) {
        mApi.callDialogGet(userId).subscribe(object : Observer<History> {
            override fun onCompleted() {

            }

            override fun onNext(history: History?) {
                if (history != null) {
                    Debug.log("FUCKING_CHAT " + history.items.count())
                }
            }

            override fun onError(e: Throwable?) {
                if (e != null) {
                    Debug.log("FUCKING_CHAT " + e.message)
                }
            }

        })
    }

    fun onComplain() {
        val immutableUserId = mUser?.id
        if (navigator != null && immutableUserId != null) {
            navigator?.showComplainScreen(immutableUserId, isNeedResult = true)
        }
    }

    fun onBlock() {
        // getOverflowMenu().processOverFlowMenuItem(OverflowMenu.OverflowMenuItem.ADD_TO_BLACK_LIST_ACTION)
        isComplainVisibile.set(View.GONE)
        // getActivity().finish()
    }

    fun onClose() {
        isComplainVisibile.set(View.GONE)
    }

    fun onMessage() {

    }

    fun onGift() {
        val immutableNavigator = navigator
        val immutableUserId = mUser?.id
        if (immutableNavigator != null && immutableUserId != null) {
            immutableNavigator.showGiftsActivity(immutableUserId, "chat")
        } else {
            Unit
        }
    }

    override fun unbind() {
        navigator = null
    }

    override fun release() {
        arrayOf(mUpdateHistorySubscription, mDialogGetSubscription, mNewMessageBroabcastSubscription,
                mVipBoughtBroabcastSubscription, pullToRefreshSubscription).safeUnsubscribe()
    }
}
