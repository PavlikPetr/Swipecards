package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.scruffy.utils.toJson
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.Api
import com.topface.topface.api.responses.History
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.api.responses.isFriendItem
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Gift
import com.topface.topface.data.Profile
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.ComplainsActivity
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.fragments.feed.FeedFragment
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.ChatData
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.CountersManager
import com.topface.topface.utils.Utils
import com.topface.topface.utils.actionbar.OverflowMenu
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.showLongToast
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.RxObservableField
import com.topface.topface.utils.rx.observeBroabcast
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.collections.forEachReversedByIndex
import org.json.JSONObject
import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ChatViewModel(private val mContext: Context, private val mApi: Api, private val mEventBus: EventBus, private val mState: TopfaceAppState) : BaseViewModel() {

    companion object {
        private const val DEFAULT_CHAT_UPDATE_PERIOD = 10000
        private const val EMPTY = ""
        private const val MUTUAL_SYMPATHY = 7
        private const val LOCK_CHAT = 35
        private const val LOCK_MESSAGE_SEND = 36
        private const val SEND_MESSAGE = "send_message"
        private const val INTENT_USER_ID = "user_id"
        const val LAST_ITEM_ID = "last id"
    }

    internal var navigator: FeedNavigator? = null
    internal var chatResult: IChatResult? = null
    internal var overflowMenu: OverflowMenu? = null
    internal var activityFinisher: IActivityFinisher? = null

    val isComplainVisible = ObservableInt(View.VISIBLE)
    val isChatVisible = ObservableInt(View.VISIBLE)
    val isButtonsEnable = ObservableBoolean(false)
    val message = RxObservableField<String>(Utils.EMPTY)
    val chatData = ChatData()
    var updateObservable: Observable<Bundle>? = null
    private var mDialogGetSubscription = AtomicReference<Subscription>()
    private var mSendMessageSubscription: CompositeSubscription = CompositeSubscription()
    private var mMessageChangeSubscription: Subscription? = null
    private var mUpdateHistorySubscription: Subscription? = null
    private var mComplainSubscription: Subscription? = null
    private var mHasPremiumSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null

    private var mUser: FeedUser? = null

    /**
     * Флаг говорящий о том, что етсть итемы с id = 0, и их нужно удалить и заменить на нормальные
     * при следующем update
     */
    private var mHasStubItems = false
    private var mIsPremium = false
    private var mIsNeedToShowToPopularPopup = false
    private var mIsNeedToBlockChat = false
    private var mIsNeedToDeleteMutualStub = false
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

        mMessageChangeSubscription = message.asRx.subscribe(shortSubscription {
            isButtonsEnable.set(it.isNotBlank() && !mIsNeedToBlockChat)
        })
        mUpdateHistorySubscription = Observable.merge(
                createGCMUpdateObservable(),
                createTimerUpdateObservable(),
                createVipBoughtObservable(),
                adapterUpdateObservable
                /*,createP2RObservable()*/).
                filter { it.first > 0 }.
                filter { mDialogGetSubscription.get()?.isUnsubscribed ?: true }.
                subscribe(shortSubscription {
                    Debug.log("FUCKING_CHAT some update from merge $it")
                    update(it)
                })
        mComplainSubscription = mEventBus.getObservable(ChatComplainEvent::class.java).subscribe(shortSubscription {
            mUser?.id?.let { id -> navigator?.showComplainScreen(id, it.itemPosition.toString()) }
        })
        mHasPremiumSubscription = mState.getObservable(Profile::class.java)
                .distinctUntilChanged { t1, t2 -> t1.premium == t2.premium }
                .subscribe(shortSubscription {
                    mIsPremium = it.premium
                    if (mIsPremium) {
                        mIsNeedToShowToPopularPopup = false
                    }
                })
        mDeleteSubscription = mApi.observeDeleteMessage()
                .doOnError { R.string.cant_delete_fake_item.getString().showLongToast() }
                .retry()
                .subscribe(shortSubscription {
                    deleteComplete ->
                    removeByPredicate { deleteComplete.items.contains(it.id) }
                    chatResult?.setResult(createResultIntent())
                })
    }

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
            removeByPredicate {it.id == 0 || it.type == MUTUAL_SYMPATHY || it.type == LOCK_CHAT }
        }
    }

    private inline fun removeByPredicate(predicate: (HistoryItem) -> Boolean) {
        if (chatData.isNotEmpty()) {
            val iterator = chatData.listIterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item is HistoryItem && predicate(item)) {
                    updateNearAvatarBeforeDelete(chatData.indexOf(item))
                    iterator.remove()
                }
            }
        }
    }

    private fun updateNearAvatarBeforeDelete(position: Int) {
        if (position > 0) {
            (chatData[position] as? HistoryItem)?.let { currentItem ->
                if (currentItem.isFriendItem() && currentItem.isDividerVisible.get()) {
                    (chatData[position - 1] as? HistoryItem)?.let { prevItem ->
                        if (prevItem.isFriendItem()) prevItem.isAvatarVisible.set(true)
                    }
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
                    if (it?.items?.isNotEmpty() ?: false) {
                        val items = ArrayList<HistoryItem>()
                        it.items.forEach {
                            when (it.type) {
                                LOCK_CHAT, MUTUAL_SYMPATHY -> mHasStubItems = true
                            }
                            if (it.type != LOCK_CHAT) {
                                items.add(wrapHistoryItem(it))
                            }
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
                }
                )))
    }

/*                          Условия показов заглушек и попапа-заглушки.
*    Первоначально проверяем на наличие итемов в History, которые пришли с сервера и наличие итемов в уже существующем списке
*      Заглушку "У вас взаимная симпатия. Напишите первым!" показываем когда:
*          1) У юзера взаимная симпатия и он начинает диалог
*    Если сообщения все-таки есть, то смотрим по типам сообщений, ктороые могу приходить
*      Заглушку "У вас взаимная симпатия. Напишите первым!" показываем когда:
*          1) У юзера нет премиума и ему приходит тип сообщения "mutual_symphaty"
*
*    Раз в сутки в рамках эксперимента 57-2 приходит сообщения из базы бомб от популярного пользователя
*      Заглушку "Юзер очень популярен, купите VIP, чтобы написать ему" показываем когда:
*          1) У юзера нет premium и ему приходит тип сообщения "LOCK_CHAT"(оно не показывается в чате)
*      Попап-заглушку "Юзер очень популярен, купите VIP, чтобы написать ему" показываем когда:
*          1) У юзера нет premium и ему приходит тип сообщения "LOCK_MESSAGE_SEND"(Оно показыватся в чате). Далее, при попытке ответить на это сообщение мы показываем попап-заглушку
*/

    private fun setStubsIfNeed(history: History) {
        var stub: Any? = null
        if (history.items.isEmpty() && chatData.isEmpty()) {
            if (history.mutualTime != 0) {
                mIsNeedToDeleteMutualStub = true
                stub = MutualStub()
            } else if (!mIsPremium) {
                mIsNeedToBlockChat = true
                stub = NotMutualBuyVipStub()
            }
        }
        if (history.items.isNotEmpty() && chatData.isEmpty() && !mIsPremium && !mHasStubItems) {
            history.items.forEach {
                stub = when (it.type) {
                    MUTUAL_SYMPATHY -> {
                        mIsNeedToDeleteMutualStub = true
                        MutualStub()
                    }
                    LOCK_CHAT -> {
                        mIsNeedToBlockChat = true
                        BuyVipStub()
                    }
                    LOCK_MESSAGE_SEND -> {
                        mIsNeedToShowToPopularPopup = true
                        mHasStubItems = true
                        null
                    }
                    else -> null
                }
            }
        }
        stub?.let { chatData.add(stub) }
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
        overflowMenu?.processOverFlowMenuItem(OverflowMenu.OverflowMenuItem.ADD_TO_BLACK_LIST_ACTION)
        isComplainVisible.set(View.GONE)
        activityFinisher?.finish()
    }

    fun onClose() = isComplainVisible.set(View.GONE)

    /**
     * В ответе приходит HistoryItem, id которого 0 так как на сервере очередь сообщений
     */
    fun onMessage() = mUser?.let {
        val message = message.get()
        if (!mIsNeedToShowToPopularPopup) {
            mSendMessageSubscription.add(mApi.callSendMessage(it.id, message)
                    .doOnSubscribe {
                        mHasStubItems = true
                        mIsSendMessage = true
                        if (mIsNeedToDeleteMutualStub){
                            chatData.clear()
                        }
                        chatData.add(0, wrapHistoryItem(HistoryItem(text = message,
                                created = System.currentTimeMillis() / 1000L)))
                        this.message.set(EMPTY)
                    }
                    .subscribe(shortSubscription({
                        Debug.log("FUCKING_CHAT send fail")
                    }, {
                        chatResult?.setResult(createResultIntent())
                    })))
        } else if (!mIsPremium) {
            mUser?.let {
                navigator?.showUserIsTooPopularLock(it)
            }
        }
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
        when (requestCode) {
            GiftsActivity.INTENT_REQUEST_GIFT -> {
                if (resultCode == Activity.RESULT_OK) {
                    isComplainVisible.set(View.INVISIBLE)
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
            }
            ComplainsActivity.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    isComplainVisible.set(View.INVISIBLE)
                    // after success complain sent - block user
                    onBlock()
                }
            }
            else -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    activityFinisher?.finish()
                }
            }
        }
    }

    /**
     * Новые модели только на этом экране, чтоб работал старый код нужен этот костыль
     */
    private fun toOldHistoryItem(item: HistoryItem?) = item?.let {
        com.topface.topface.data.History(JSONObject(it.toJson()))
    }

    internal fun createResultIntent() = Intent().apply {
        putExtra(ChatActivity.LAST_MESSAGE, toOldHistoryItem(getFirstCorrectItemId()))
        putParcelableArrayListExtra(ChatActivity.DISPATCHED_GIFTS, mDispatchedGifts)
        putExtra(SEND_MESSAGE, mIsSendMessage)
        putExtra(INTENT_USER_ID, mUser?.id ?: -1)
    }

    override fun unbind() {
        chatResult = null
        navigator = null
        overflowMenu = null
        activityFinisher = null
    }

    override fun release() {
        mDialogGetSubscription.get().safeUnsubscribe()
        arrayOf(mSendMessageSubscription, mMessageChangeSubscription, mUpdateHistorySubscription,
                mComplainSubscription, mHasPremiumSubscription, mDeleteSubscription).safeUnsubscribe()
    }

}
