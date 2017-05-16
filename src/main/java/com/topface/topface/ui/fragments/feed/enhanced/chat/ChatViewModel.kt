package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.api.Api
import com.topface.topface.api.responses.History
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Gift
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.state.EventBus
import com.topface.topface.ui.ComplainsActivity
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.fragments.feed.FeedFragment
import com.topface.topface.ui.fragments.feed.enhanced.IChatResult
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.ChatData
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.CountersManager
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.RxObservableField
import com.topface.topface.utils.rx.observeBroabcast
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.collections.forEachReversedByIndex
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ChatViewModel(private val mContext: Context, private val mApi: Api, private val mEventBus: EventBus) : BaseViewModel() {

    companion object {
        private const val DEFAULT_CHAT_UPDATE_PERIOD = 30000
        private const val EMPTY = ""
        private const val MUTUAL_SYMPATHY = 7
        private const val LOCK_CHAT = 35
        private const val SEND_MESSAGE = "send_message"
        private const val INTENT_USER_ID = "user_id"
        const val LAST_ITEM_ID = "last id"
    }

    internal var navigator: FeedNavigator? = null
    internal var chatResult: IChatResult? = null

    val isComplainVisibile = ObservableInt(View.VISIBLE)
    val isChatVisible = ObservableInt(View.VISIBLE)
    val message = RxObservableField<String>()
    val chatData = ChatData()
    var updateObservable: Observable<Bundle>? = null
    private var mDialogGetSubscription = AtomicReference<Subscription>()
    private var mSendMessageSubscription: Subscription? = null
    private var mUpdateHistorySubscription: Subscription? = null
    private var mComplainSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null

    private var mUser: FeedUser? = null

    /**
     * Флаг говорящий о том, что етсть итемы с id = 0, и их нужно удалить и заменить на нормальные
     * при следующем update
     */
    private var mHasStubItems = false
    private var mIsNeedShowAddPhoto = true
    /**
     * Коллекция отправленных из чатика подарочков. Нужны, чтобы обновльты изтем со списком
     * подарочков юзера в дейтинге. Как только дейтинг будет переделан на новый скраффи, там сразу
     * можно будет лофить ивент об успешном отправлении подарочка, и сразу его добавлять
     */
    private var mDispatchedGifts: ArrayList<Gift> = ArrayList()
    private var mIsSendMessage = false

    override fun bind() {
        mUser = args?.getParcelable(ChatIntentCreator.WHOLE_USER)
        val user = mUser
        if (user?.photo != null && user.photo.isEmpty) {
            takePhotoIfNeed()
        }
        val adapterUpdateObservable = updateObservable
                ?.distinct { it.getInt(LAST_ITEM_ID) }
                ?.map { createUpdateObject(mUser?.id ?: -1) }
                ?: Observable.empty()

        mUpdateHistorySubscription = Observable.merge(
                createGCMUpdateObservable(),
                createTimerUpdateObservable(),
                createVipBoughtObservable(),
                adapterUpdateObservable,
                createDeleteObservable()
                /*,createP2RObservable()*/).
                filter { it.first > 0 }.
                filter { mDialogGetSubscription.get()?.isUnsubscribed ?: true }.
                subscribe(shortSubscription {
                    Debug.log("FUCKING_CHAT some update from merge $it")
                    update(it)
                })
        mComplainSubscription = mEventBus.getObservable(ChatComplainEvent::class.java).subscribe(shortSubscription {
            onComplain()
        })
    }

    private fun createDeleteObservable() = mApi.observeDeleteMessage()
            .filter { it.completed }
            .map { createUpdateObject(mUser?.id ?: -1) }

    private fun createVipBoughtObservable() = mContext.observeBroabcast(IntentFilter(CountersManager.UPDATE_VIP_STATUS))
            .filter { it.getBooleanExtra(CountersManager.VIP_STATUS_EXTRA, false) }
            .map {
                chatData.clear()
                createUpdateObject(mUser?.id ?: -1)
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
            interval(DEFAULT_CHAT_UPDATE_PERIOD.toLong(), DEFAULT_CHAT_UPDATE_PERIOD.toLong(), TimeUnit.MILLISECONDS)
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
                val to = getLastCorrectItemId()?.id.toString()
                Triple<Int, String?, String?>(userId, null, to)
            } else {
                val from = getFirstCorrectItemId()?.id.toString()
                Triple<Int, String?, String?>(userId, from, null)
            }

    /**
     * Ищем последний id итема чата не равный 0, чтоб от него запрсить новые итемы,
     * которые были в очереди
     */
    private fun getLastCorrectItemId(): HistoryItem? {
        if (chatData.isNotEmpty()) {
            chatData.forEachReversedByIndex {
                if (it is HistoryItem && it.id != 0) {
                    return it
                }
            }
        }
        return null
    }

    /**
     * Ищем первый id итема чата не равный 0, чтоб от него запрсить новые итемы,
     * которые мало ли где были
     */
    private fun getFirstCorrectItemId(): HistoryItem? {
        if (chatData.isNotEmpty()) {
            chatData.forEach {
                if (it is HistoryItem && it.id != 0) {
                    return it
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
        if (mHasStubItems) {
            mHasStubItems = false
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
        mDialogGetSubscription.set(mApi.callDialogGet(updateContainer.first, updateContainer.second, updateContainer.third)
                .subscribe(shortSubscription({
                    mDialogGetSubscription.get()?.unsubscribe()
                }, {
                    setStubsIfNeed(it)
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
                    }
                    mDialogGetSubscription.get()?.unsubscribe()
                    Debug.log("FUCKING_CHAT " + it.items.count())
                })))
    }

    private fun setStubsIfNeed(history: History) {
        if (history.items.isEmpty() && history.mutualTime != 0) {
            chatData.add(MutualStub())
            mHasStubItems = true
        }
        if (!App.get().profile.premium) {
            history.items.forEach {
                when (it.type) {
                    MUTUAL_SYMPATHY -> {
                        MutualStub()
                        mHasStubItems = true
                    }
                    LOCK_CHAT -> {
                        BuyVipStub()
                        mHasStubItems = true
                    }
                }
            }
        }
    }


    /**
     * Запаковать итем в соответствующую модель чата, дабы работало приведение в базовом компоненте
     */
    private fun wrapHistoryItem(item: HistoryItem) = when (item.getItemType()) {
        HistoryItem.USER_MESSAGE -> UserMessage(item)
        HistoryItem.FRIEND_MESSAGE -> FriendMessage(item)
        HistoryItem.FRIEND_GIFT -> FriendGift(item)
        HistoryItem.USER_GIFT -> UserGift(item)
        else -> item
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

    fun onClose() = isComplainVisibile.set(View.GONE)

    /**
     * В ответе приходит HistoryItem, id которого 0 так как на сервере очередь сообщений
     */
    fun onMessage() = mUser?.let {
        mSendMessageSubscription = mApi.callSendMessage(it.id, message.get()).subscribe(shortSubscription({
            Debug.log("FUCKING_CHAT send fail")
        }, {
            it?.let {
                mHasStubItems = true
                mIsSendMessage = true
                chatData.add(0, wrapHistoryItem(it))
                message.set(EMPTY)
                chatResult?.setResult(createResultIntent())
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

    private fun giftAnswerToHistoryItem(answer: SendGiftAnswer): UserGift? {
        return answer.history?.let {
            UserGift(HistoryItem(it.text, 0f, 0f, it.type, it.id.toIntOrNull() ?: 0, it.created,
                    it.target, it.unread, it.link))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GiftsActivity.INTENT_REQUEST_GIFT -> {
                    isComplainVisibile.set(View.INVISIBLE)
                    data?.extras?.let {
                        val sendGiftAnswer = it.getParcelable<SendGiftAnswer>(GiftsActivity.INTENT_SEND_GIFT_ANSWER)
                        giftAnswerToHistoryItem(sendGiftAnswer)?.let {
                            mHasStubItems = true
                            mIsSendMessage = true
                            chatData.add(0, it)
                            chatResult?.setResult(createResultIntent())
                        }
                        if (sendGiftAnswer.history != null && sendGiftAnswer.history.mJsonForParse != null) {
                            mDispatchedGifts.add(0, JsonUtils.fromJson(sendGiftAnswer.history.mJsonForParse, Gift::class.java))
                        }
                        LocalBroadcastManager.getInstance(mContext)
                                .sendBroadcast(Intent(FeedFragment.REFRESH_DIALOGS))
                    }
                }
                ComplainsActivity.REQUEST_CODE -> {
                    isComplainVisibile.set(View.INVISIBLE)
                    // after success complain sent - block user
                    onBlock()
                }
            }
        }
    }

    /**
     * Новые модели только на этом экране, чтоб работал старый код нужен этот костыль
     */
    private fun toOldHistoryItem(item: HistoryItem?) = item?.let {
        com.topface.topface.data.History().apply {
            text = it.text
            type = it.type
            id = it.id.toString()
            created = it.created
            target = it.target
            unread = it.unread
            link = it.link
        }
    }

    internal fun createResultIntent() = Intent().apply {
        putExtra(ChatActivity.LAST_MESSAGE, toOldHistoryItem(getLastCorrectItemId()))
        putParcelableArrayListExtra(ChatActivity.DISPATCHED_GIFTS, mDispatchedGifts)
        putExtra(SEND_MESSAGE, mIsSendMessage)
        putExtra(INTENT_USER_ID, mUser?.id ?: -1)
    }

    override fun unbind() {
        chatResult = null
        navigator = null
    }

    override fun release() {
        mDialogGetSubscription.get().safeUnsubscribe()
        arrayOf(mSendMessageSubscription, mUpdateHistorySubscription,
                mDeleteSubscription, mComplainSubscription).safeUnsubscribe()
    }

}
