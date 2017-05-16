package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.content.Context
import android.content.IntentFilter
import android.databinding.ObservableInt
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.api.Api
import com.topface.topface.api.responses.History
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.data.FeedUser
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.ChatData
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.showLongToast
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.RxObservableField
import com.topface.topface.utils.rx.observeBroabcast
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.collections.forEachReversedByIndex
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit

class ChatViewModel(private val mContext: Context, private val mApi: Api) : BaseViewModel() {

    companion object {
        private const val DEFAULT_CHAT_UPDATE_PERIOD = 30000
        private const val EMPTY = ""
        val MUTUAL_SYMPATHY = 7
        val LOCK_CHAT = 35
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

    /**
     * Флаг говорящий о том, что етсть итемы с id = 0, и их нужно удалить и заменить на нормальные
     * при следующем update
     */
    private var hasStubItems = false
    private var mIsNeedShowAddPhoto = true

    override fun bind() {
        mUser = args?.getParcelable(ChatIntentCreator.WHOLE_USER)
        val user = mUser
        if (user != null && user.photo.isEmpty) {
            takePhotoIfNeed()
        }
        mUpdateHistorySubscription = Observable.merge(
                createGCMUpdateObservable(),
                createTimerUpdateObservable()
                /*,createP2RObservable()*/).
                filter { it.first > 0 }.
                subscribe(shortSubscription {
                    Debug.log("FUCKING_CHAT some update from merge $it")
                    update(it)
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
                        createUpdateObject(it.first)
                    }

    private fun createTimerUpdateObservable() = Observable.
            interval(0, DEFAULT_CHAT_UPDATE_PERIOD.toLong(), TimeUnit.MILLISECONDS)
            .map { createUpdateObject(mUser?.id ?: -1) }

    //todo заменить при имплементацию птр
    //private fun createP2RObservable() = Observable.just(createUpdateObject(-1))

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


    private fun createUpdateObject(userId: Int, isBottom: Boolean = false) =
            if (isBottom) {
                val to = getLastCorrectItemId()
                Triple<Int, String?, String?>(userId, null, to)
            } else {
                val from = getFirstCorrectItemId()
                Triple<Int, String?, String?>(userId, from, null)
            }

    /**
     * Ищем последний id итема чата не равный 0, чтоб от него запрсить новые итемы,
     * которые были в очереди
     */
    private fun getLastCorrectItemId(): String? {
        if (chatData.isNotEmpty()) {
            chatData.forEachReversedByIndex {
                if (it is HistoryItem && it.id != 0) {
                    return it.id.toString()
                }
            }
        }
        return null
    }

    /**
     * Ищем первый id итема чата не равный 0, чтоб от него запрсить новые итемы,
     * которые мало ли где были
     */
    private fun getFirstCorrectItemId(): String? {
        if (chatData.isNotEmpty()) {
            chatData.forEach {
                if (it is HistoryItem && it.id != 0) {
                    return it.id.toString()
                }
            }
        }
        return null
    }

    /**
     * Удаляем итемы с id = 0, т.к. на данный момент у нас есть нормальные итемы,
     * которыми можно заменить заглушки(ну так серверные говрят по крайней мере)
     */
    private fun removeStubItems() {
        if (hasStubItems) {
            hasStubItems = false
            val iterator = chatData.listIterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item is HistoryItem && item.id == 0) {
                    iterator.remove()
                }
            }
        }
    }


    /**
     * Обновление по эмитам гцм, птр, таймера
     */
    private fun update(updateContainer: Triple<Int, String?, String?>) {
        val addToStart = updateContainer.second != null
        mApi.callDialogGet(updateContainer.first, updateContainer.second, updateContainer.third)
                .subscribe(shortSubscription({
                    "Тобi пiзда".showLongToast()
                }, {
                    if (it != null && it.items.isNotEmpty()) {
                        val items = ArrayList<HistoryItem>()
                        it.items.forEach {
                            items.add(wrapHistoryItem(it))
                        }
                        removeStubItems()
                        if (addToStart) {
                            chatData.addAll(0, items)
                        } else {
                            chatData.addAll(items)
                        }
                        setStubsIfNeed(it)
                    }

                    Debug.log("FUCKING_CHAT " + it.items.count())
                }))
    }

    private fun setStubsIfNeed( history: History){
        if (history.items.isEmpty() && history.mutualTime != 0) {
            Debug.error("             if (history.items.isEmpty() && history.mutualTime != 0)                           показ Взаимной заглушки")
            chatData.add(MutualStub())
        }
        if (!App.get().profile.premium) {
            Debug.error("             if (!App.get().profile.premium)                         ")
            Debug.error("")
            for (item in history.items) {
                Debug.error("            и тип ${item.type}")
                when (item.type) {
                    MUTUAL_SYMPATHY -> {
                        Debug.error("                показ Взаимной заглушки")
                        MutualStub()
                    }
                    LOCK_CHAT -> {
                        Debug.error("                показ покупка вип")
                        BuyVipStub()}
                }
            }
        }
    }


    /**
     * Запаковать итем в соответствующую модель чата, дабы работало приведение в базовом компоненте
     */
    private fun wrapHistoryItem(item: HistoryItem): HistoryItem {
        if (item.getItemType() == HistoryItem.USER_MESSAGE) {
            return UserMessage(item)
        }
        if (item.getItemType() == HistoryItem.FRIEND_MESSAGE) {
            return FriendMessage(item)
        }
        return item
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

    /**
     * В ответе приходит HistoryItem, id которого 0 так как там очередь сообщений
     */
    fun onMessage() {
        mApi.callSendMessage(mUser!!.id, message.get()).subscribe(shortSubscription({
            Debug.log("FUCKING_CHAT send fail")
        }, {
            if (it != null) {
                hasStubItems = true
                chatData.add(0, wrapHistoryItem(it))
                message.set(EMPTY)
            }
        }))
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

    override fun release() = arrayOf(mUpdateHistorySubscription, mDialogGetSubscription,
            mNewMessageBroabcastSubscription, mVipBoughtBroabcastSubscription,
            pullToRefreshSubscription).safeUnsubscribe()

}
