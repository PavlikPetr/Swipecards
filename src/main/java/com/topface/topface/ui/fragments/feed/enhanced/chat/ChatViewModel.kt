package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.topface.framework.JsonUtils
import com.topface.scruffy.utils.toJson
import com.topface.statistics.android.Slices
import com.topface.statistics.generated.ChatStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.Api
import com.topface.topface.api.responses.History
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.chat.IComplainHeaderActionListener
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Gift
import com.topface.topface.data.Profile
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.ComplainsActivity
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.PurchasesActivity
import com.topface.topface.ui.fragments.feed.FeedFragment
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatIntentCreator.FROM
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatStatistics.START_CHAT_FROM
import com.topface.topface.ui.fragments.feed.enhanced.chat.items.prepareAvatars
import com.topface.topface.ui.fragments.feed.enhanced.chat.items.prepareDividers
import com.topface.topface.ui.fragments.feed.enhanced.utils.ChatData
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.CountersManager
import com.topface.topface.utils.Utils
import com.topface.topface.utils.actionbar.OverflowMenu
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.showLongToast
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.RxObservableField
import com.topface.topface.utils.rx.observeBroadcast
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

class ChatViewModel(private val mApi: Api, private val mEventBus: EventBus,
                    private val mState: TopfaceAppState) : BaseViewModel() {

    companion object {
        private const val DEFAULT_CHAT_UPDATE_PERIOD = 10000L
        private const val DEFAULT_CHAT_INIT_PERIOD = 300L
        private const val EMPTY = ""
        private const val MUTUAL_SYMPATHY = 7
        private const val LOCK_CHAT = 35
        private const val LOCK_MESSAGE_SEND = 36
        private const val SEND_MESSAGE = "send_message"
        private const val INTENT_USER_ID = "user_id"
        const val SERVER_TIME_CORRECTION = 1000L
        const val LAST_ITEM_ID = "last id"

        const val UNDEFINED = -1
        const val SOMETHING_WRONG = -2
        const val NO_BLOCK = 0
        const val MUTUAL_SYMPATHY_LOCK = MUTUAL_SYMPATHY
        const val MUTUAL_SYMPATHY_STUB = 8
        const val NO_MUTUAL_NO_VIP_STUB = 9
        const val LOCK_CHAT_STUB = LOCK_CHAT
        const val LOCK_MESSAGE_FOR_SEND = LOCK_MESSAGE_SEND

    }

    internal var navigator: FeedNavigator? = null
    internal var chatResult: IChatResult? = null
    internal var overflowMenu: OverflowMenu? = null
    internal var activityFinisher: IActivityFinisher? = null
    private var mStartChatFrom: String? = null

    val complainVisibility
        get() = App.getAppComponent().suspiciousUserCache().getIsUserSuspicious(mUser?.id ?: 0)
    val isChatVisible = ObservableInt(View.VISIBLE)
    val isSendButtonEnable = ObservableBoolean(false)
    val isSendGiftEnable = ObservableBoolean(false)
    val isEditTextEnable = ObservableBoolean(false)
    val message = RxObservableField<String>(Utils.EMPTY)
    val chatData = ChatData()
    private var mDialogGetSubscription = AtomicReference<Subscription>()
    private var mSendMessageSubscription: CompositeSubscription = CompositeSubscription()
    private var mMessageChangeSubscription: Subscription? = null
    private var mUpdateHistorySubscription: Subscription? = null
    private var mComplainSubscription: Subscription? = null
    private var mResendSubscription: Subscription? = null
    private var mHasPremiumSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null
    private var mUpdateAdapterSubscription: Subscription? = null

    private var mUser: FeedUser? = null

    /**
     * Флаг говорящий о том, что етсть итемы с id = 0, и их нужно удалить и заменить на нормальные
     * при следующем update
     */
    private var mHasStubItems = false
    private var mIsPremium = false
    private var mIsNeedShowAddPhoto = true
    private var mBlockChatType: Int = UNDEFINED

    /**
     * Флаг запоминающий надо или нет показывать в обрамлении самого старого элемента чата
     * текст "Взаимная симпатия"
     */
    private var mIsNeedShowMutualDivider = false

    /**
     * Коллекция отправленных из чатика подарочков. Нужны, чтобы обновльты изтем со списком
     * подарочков юзера в дейтинге. Как только дейтинг будет переделан на новый скраффи, там сразу
     * можно будет лофить ивент об успешном отправлении подарочка, и сразу его добавлять
     */
    private var mDispatchedGifts: ArrayList<Gift> = ArrayList()
    private var mIsSendMessage = false

    val complainHeaderActionListener = object : IComplainHeaderActionListener {
        override fun onComplain() {
            val immutableUserId = mUser?.id
            if (navigator != null && immutableUserId != null) {
                navigator?.showComplainScreen(immutableUserId, isNeedResult = true)
            }
        }

        override fun onBlock() {
            overflowMenu?.processOverFlowMenuItem(OverflowMenu.OverflowMenuItem.ADD_TO_BLACK_LIST_ACTION)
            hideComplainHeader()
            activityFinisher?.finish()
        }

        override fun onClose() = hideComplainHeader()
    }

    init {
        mUpdateHistorySubscription = Observable.merge(
                createGCMUpdateObservable(),
                createTimerUpdateObservable(),
                createVipBoughtObservable()
                /*,createP2RObservable()*/)
                .filter { params -> params?.let { return@let it.first == mUser?.id } ?: false }
                .filter { mDialogGetSubscription.get()?.isUnsubscribed ?: true }
                .subscribe(shortSubscription { update(it) })
        mComplainSubscription = mEventBus.getObservable(ChatComplainEvent::class.java).subscribe(shortSubscription {
            mUser?.id?.let { id -> navigator?.showComplainScreen(id, it.itemPosition.toString()) }
        })

        mResendSubscription = mEventBus.getObservable(SendHistoryItemEvent::class.java).subscribe(shortSubscription {
            HistoryItemSender.send(mSendMessageSubscription, mApi, it.item, mUser?.id ?: 0,
                    {
                        mHasStubItems = true
                        mIsSendMessage = true
                    },
                    {
                        chatResult?.setResult(createResultIntent())
                    }
            )
        })

        mHasPremiumSubscription = mState.getObservable(Profile::class.java)
                .distinctUntilChanged { t1, t2 -> t1.premium == t2.premium }
                .subscribe(shortSubscription { mIsPremium = it.premium })
        mDeleteSubscription = mApi.observeDeleteMessage()
                .doOnError { R.string.cant_delete_fake_item.getString().showLongToast() }
                .retry()
                .subscribe(shortSubscription {
                    deleteComplete ->
                    removeByPredicate { deleteComplete.items.contains(it.id) }
                    chatResult?.setResult(createResultIntent())
                })
    }

    fun initUpdateAdapterSubscription(updateObservable: Observable<Bundle>?) {
        mUpdateAdapterSubscription = updateObservable
                ?.distinct { it.getInt(LAST_ITEM_ID) }
                ?.map { createUpdateObject(mUser?.id ?: -1, true) }
                ?.filter { params -> params?.let { return@let it.first == mUser?.id } ?: false }
                ?.filter { mDialogGetSubscription.get()?.isUnsubscribed ?: true }
                ?.subscribe(shortSubscription {
                    update(it)
                })
    }

    override fun bind() {
        if (mBlockChatType == UNDEFINED) {
            chatData.add(ChatLoader())
        }
        mUser = args?.getParcelable(ChatIntentCreator.WHOLE_USER)
        mStartChatFrom = args?.getString(FROM, "undefined")
        ChatStatisticsGeneratedStatistics.sendNow_CHAT_SHOW(Slices().putSlice(START_CHAT_FROM, mStartChatFrom))
        takePhotoIfNeed()
        mMessageChangeSubscription = message.asRx.subscribe(shortSubscription {
            isSendButtonEnable.set(it.isNotBlank())
        })
    }

    private fun createVipBoughtObservable() = observeBroadcast(IntentFilter(CountersManager.UPDATE_VIP_STATUS))
            .filter { it.getBooleanExtra(CountersManager.VIP_STATUS_EXTRA, false) }
            .map {
                chatData.clear()
                createUpdateObject(mUser?.id ?: -1)
            }

    private fun createGCMUpdateObservable() = observeBroadcast(IntentFilter(GCMUtils.GCM_NOTIFICATION))
            .map {
                val id = try {
                    Integer.parseInt(it.getStringExtra(GCMUtils.USER_ID_EXTRA))
                } catch (e: NumberFormatException) {
                    -1
                }
                val type = it.getIntExtra(GCMUtils.GCM_TYPE, GCMUtils.GCM_TYPE_UNKNOWN)
                Pair(id, type)
            }
            .filter {
                it.first != -1 && mUser?.id == it.first
            }
            .map {
                GCMUtils.cancelNotification(it.second)
                createUpdateObject(it.first)
            }

    private fun createTimerUpdateObservable() = Observable.
            interval(DEFAULT_CHAT_INIT_PERIOD, DEFAULT_CHAT_UPDATE_PERIOD, TimeUnit.MILLISECONDS)
            .map { createUpdateObject(mUser?.id ?: -1) }

    //todo заменить при имплементацию птр
    //private fun createP2RObservable() = Observable.just(createUpdateObject(-1))

    private fun takePhotoIfNeed() {
        val photo = App.get().profile.photo
        if (mIsNeedShowAddPhoto && (photo == null || photo.isEmpty)) {
            mIsNeedShowAddPhoto = false
            if (isTakePhotoApplicable()) {
                navigator?.showTakePhotoPopup()
            }
        }
    }

    private fun isTakePhotoApplicable() = !App.getConfig().userConfig.isUserAvatarAvailable &&
            App.get().profile.photo == null

    /**
     * Больше дичи в обновления
     * сей метод вернет набор userId, from, to, либо null, если изначальный список пуст
     * и обновление "дергается" снизу экрана, в том числе всякими таймерами/пушами
     * если обновление дергается сверху, и список пуст, то будет сгенерирован "первичный" запрос,
     * без указания from/to и в дальнейшем, в методе update это будет учтено, чтобы запрос не "прочитал"
     *
     * @return params for server Triple<userId, from, to>?
     */
    private fun createUpdateObject(userId: Int, isBottom: Boolean = false) =
            if (isBottom) {
                getLastCorrectItemId()?.let {
                    return@let Triple<Int, String?, String?>(userId, null, it.id.toString())
                } ?: Triple<Int, String?, String?>(userId, null, null)
            } else {
                getFirstCorrectItemId()?.let {
                    return@let Triple<Int, String?, String?>(userId, it.id.toString(), null)
                }
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
     * Удаляем итемы с id = 0 (и только если они не отсылаются в данный момент),
     * т.к. на данный момент у нас есть нормальные итемы,
     * которыми можно заменить заглушки(ну так серверные говрят по крайней мере)
     */
    private fun removeStubItems() {
        if (mHasStubItems) {
            mHasStubItems = false
            removeByPredicate { (it.id == 0 && !it.isSending.get()) || it.type == MUTUAL_SYMPATHY || it.type == LOCK_CHAT }
        }
    }

    private inline fun removeByPredicate(predicate: (HistoryItem) -> Boolean) {
        if (chatData.isNotEmpty()) {
            val iterator = chatData.listIterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item is HistoryItem && predicate(item)) {
                    iterator.remove()
                    chatData.filterIsInstance<HistoryItem>().prepareDividers().prepareAvatars()
                }
            }
        }
    }

    private fun isMessage(currentData: ChatData) = currentData.isNotEmpty() && (currentData.find { !it.isStubItem() } != null && mBlockChatType != LOCK_MESSAGE_FOR_SEND)

    private fun isStubs(currentData: ChatData) = currentData.isNotEmpty() && ((currentData.find { it.isStubItem() } != null))

    private fun isEmptyState(currentData: ChatData) = currentData.isEmpty()

    // новое сообщение
    private fun isNewMessage(newData: History) =
            newData.items.isNotEmpty() && newData.items.find { it.type == LOCK_MESSAGE_FOR_SEND || it.type == MUTUAL_SYMPATHY || it.type == LOCK_CHAT || it.type == LOCK_MESSAGE_SEND } == null

    // установка стабов
    private fun isNeedStubs(newData: History): Boolean = newData.items.isEmpty()

    // установка stubMessage
    private fun isNeedMessageStub(newData: History): Boolean = newData.items.isNotEmpty() && !mIsPremium && !mHasStubItems

    /**
     * Обновление по эмитам гцм, птр, таймера
     */

    private fun update(updateContainer: Triple<Int, String?, String?>?) = updateContainer?.let { (userId, from, to) ->
        val addToStart = from != null
        fun isReallyNeedLeave() = isNeedLeave() || (from == null && to == null)
        mDialogGetSubscription.set(mApi.callDialogGet(userId, from, to, isReallyNeedLeave())
                .map {
                    mUser = FeedUser.createFeedUserFromUser(it.user)
                    if (mBlockChatType == UNDEFINED) {
                        isSendGiftEnable.set(true)
                        isEditTextEnable.set(true)
                        chatData.clear()
                    }
                    mIsNeedShowMutualDivider = !it.more && (it.mutualTime != 0)
                    when {
                        isMessage(chatData) -> {
                            mBlockChatType = NO_BLOCK
                            when {
                                isNewMessage(it) -> addMessages(it)
                                else -> null
                            }
                        }
                        isStubs(chatData) -> {
                            when {
                                isNewMessage(it) -> {
                                    mBlockChatType = NO_BLOCK
                                    chatData.clear()
                                    addMessages(it)
                                }
                                else -> null
                            }
                        }
                        isEmptyState(chatData) -> {
                            mBlockChatType = NO_BLOCK
                            when {
                                isNewMessage(it) -> addMessages(it)
                                isNeedStubs(it) -> getStubs(it)
                                isNeedMessageStub(it) -> getStubMessages(it)
                                else -> null
                            }
                        }
                        else -> null
                    }
                }
                .filter { it != null }
                .subscribe(shortSubscription({
                    mDialogGetSubscription.get()?.unsubscribe()
                }, {
                    chatResult?.setResult(createResultIntent())
                    setBlockSettings()
                    if (addToStart) {
                        //TODO НИЖЕ ГОВНО ПОПРАВЬ ПАРЯ
                        // сорян за это говно, но это единственный вариант без переписывания ChatData
                        // зафиксить баг с *задваиванием*, т.к. при добавлении более одного итема в начало
                        // происходит дублирование целого блока итемов
                        it?.forEachReversedByIndex { chatData.add(0, it) }

                    } else {
                        it?.let { it -> chatData.addAll(it) }
                    }
                    mDialogGetSubscription.get()?.unsubscribe()
                    // обновим превью только если запрос ушел с прочтением истории
                    if (isNeedReadFeed()) {
                        chatResult?.setResult(createResultIntent())
                    }
                    mDialogGetSubscription.get()?.unsubscribe()
                }
                )))
    }


    private fun addMessages(newData: History): ArrayList<HistoryItem> {
        App.getAppComponent().suspiciousUserCache().setUserIsSuspiciousIfNeed(newData.user.id.toInt(), newData.isSuspiciousUser)
        val items = ArrayList<HistoryItem>()
        newData.items.forEach {
            it.isMutual = mIsNeedShowMutualDivider
            items.add(wrapHistoryItem(it))
        }
        removeStubItems()
        return items
    }

    private fun getStubs(newData: History): ArrayList<IChatItem>? {
        var stub: IChatItem? = null
        if (newData.mutualTime != 0) {
            mBlockChatType = MUTUAL_SYMPATHY_STUB
            stub = MutualStub()
        } else if (!mIsPremium) {
            mBlockChatType = NO_MUTUAL_NO_VIP_STUB
            stub = NotMutualBuyVipStub()
        }
        return stub?.let { arrayListOf<IChatItem>(it) }
    }

    private fun getStubMessages(newData: History): ArrayList<IChatItem>? {
        var stub: IChatItem? = null
        val lastItem = newData.items.lastOrNull()
        stub = when (lastItem?.type) {
            MUTUAL_SYMPATHY -> {
                mBlockChatType = MUTUAL_SYMPATHY_STUB
                MutualStub()
            }
            LOCK_CHAT -> {
                mBlockChatType = LOCK_CHAT_STUB
                BuyVipStub()
            }
            LOCK_MESSAGE_SEND -> {
                mBlockChatType = LOCK_MESSAGE_FOR_SEND
                FriendMessage(lastItem)
            }
            else -> {
                mBlockChatType = SOMETHING_WRONG
                null
            }
        }
        return stub?.let { arrayListOf<IChatItem>(it) }
    }

    private fun isNeedLeave() = isTakePhotoApplicable()

    private fun isNeedReadFeed() = !isNeedLeave() && chatData.find { (it as? HistoryItem)?.type == LOCK_MESSAGE_SEND || (it as? HistoryItem)?.type == LOCK_CHAT } == null

    private fun setBlockSettings() {
        when (mBlockChatType) {
            MUTUAL_SYMPATHY_STUB, LOCK_MESSAGE_FOR_SEND, NO_BLOCK -> {
                isSendGiftEnable.set(true)
                isEditTextEnable.set(true)
            }
            NO_MUTUAL_NO_VIP_STUB, LOCK_CHAT_STUB -> {
                isEditTextEnable.set(false)
                isSendGiftEnable.set(false)
            }
            MUTUAL_SYMPATHY -> {
                isEditTextEnable.set(true)
                isSendGiftEnable.set(false)
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

    /**
     * В ответе приходит HistoryItem, id которого 0 так как на сервере очередь сообщений
     */
    fun onMessage() = mUser?.let {
        val message = message.get()
        if (mBlockChatType != LOCK_MESSAGE_FOR_SEND) {
            mSendMessageSubscription.add(mApi.callSendMessage(it.id, message)
                    .doOnSubscribe {
                        mHasStubItems = true
                        mIsSendMessage = true
                        if (mBlockChatType == MUTUAL_SYMPATHY_STUB) {
                            chatData.clear()
                            mBlockChatType = NO_BLOCK
                        }
                        if (isEmptyState(chatData) || mBlockChatType == MUTUAL_SYMPATHY_STUB) {
                            ChatStatisticsGeneratedStatistics.sendNow_CHAT_FIRST_MESSAGE_SEND(Slices().putSlice(START_CHAT_FROM, mStartChatFrom))
                        }
                        chatData.add(0, wrapHistoryItem(HistoryItem(text = message,
                                created = System.currentTimeMillis() / SERVER_TIME_CORRECTION,
                                isMutual = mIsNeedShowMutualDivider)))
                        this.message.set(EMPTY)
                    }
                    .subscribe(shortSubscription({
                    }, {
                        chatResult?.setResult(createResultIntent())
                    })))
        } else {
            navigator?.showUserIsTooPopularLock(it)
        }
    }

    fun onGift() = mUser?.let {
        if (mBlockChatType != LOCK_MESSAGE_FOR_SEND) {
            ChatStatisticsGeneratedStatistics.sendNow_CHAT_GIFT_ACTIVITY_OPEN(Slices().putSlice(START_CHAT_FROM, "chat"))
            navigator?.showGiftsActivity(it.id, "chat")
        } else {
            navigator?.showUserIsTooPopularLock(it)
        }
    }

    private fun giftAnswerToHistoryItem(answer: SendGiftAnswer): UserGift? {
        return answer.history?.let {
            UserGift(HistoryItem(it.text, 0f, 0f, it.type, it.id.toIntOrNull() ?: 0, it.created / SERVER_TIME_CORRECTION,
                    it.target, it.unread, it.link))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GiftsActivity.INTENT_REQUEST_GIFT -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (mBlockChatType == MUTUAL_SYMPATHY_STUB) {
                        chatData.clear()
                        mBlockChatType = NO_BLOCK
                    }
                    if (isEmptyState(chatData) || mBlockChatType == MUTUAL_SYMPATHY_STUB) {
                        ChatStatisticsGeneratedStatistics.sendNow_CHAT_FIRST_MESSAGE_SEND(Slices().putSlice(START_CHAT_FROM, mStartChatFrom))
                    }
                    hideComplainHeader()
                    data?.extras?.let {
                        val sendGiftAnswer = it.getParcelable<SendGiftAnswer>(GiftsActivity.INTENT_SEND_GIFT_ANSWER)
                        sendGiftAnswer.history.mJsonForParse?.let {
                            mDispatchedGifts.add(0, JsonUtils.fromJson(it, Gift::class.java))
                        }
                        giftAnswerToHistoryItem(sendGiftAnswer.apply { history.unread = false })?.let {
                            mHasStubItems = true
                            mIsSendMessage = true
                            it.isMutual = mIsNeedShowMutualDivider
                            chatData.add(0, it)
                            chatResult?.setResult(createResultIntent())
                        }
                        LocalBroadcastManager.getInstance(App.getContext())
                                .sendBroadcast(Intent(FeedFragment.REFRESH_DIALOGS))
                    }
                }
            }
            PurchasesActivity.INTENT_BUY_VIP -> {
                chatData.clear()
                isSendGiftEnable.set(true)
                isEditTextEnable.set(true)
                update(createUpdateObject(mUser?.id ?: -1))
            }
            ComplainsActivity.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // after success complain sent - block user
                    complainHeaderActionListener.onBlock()
                }
            }
            else -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    activityFinisher?.finish()
                }
            }
        }
    }

    private fun hideComplainHeader() {
        App.getAppComponent().suspiciousUserCache().setUserIsSuspicious(mUser?.id ?: 0, false)
    }

    /**
     * Новые модели только на этом экране, чтоб работал старый код нужен этот костыль
     */
    private fun toOldHistoryItem(item: HistoryItem?) = item?.let {
        // данная перепаковка с промежуточной обработкой нужна ибо
        // History внутри себя хранит json-строку из которой был собран
        // и использует ее для создания Parcelable
        // переделывать внутренности этого элемента тупо страшно
        val tempHistory = com.topface.topface.data.History(JSONObject(it.toJson())).apply {
            user = mUser
            if (it.type != LOCK_CHAT) unread = false
        }
        com.topface.topface.data.History(JSONObject(JsonUtils.toJson(tempHistory)))
    }

    internal fun createResultIntent() = Intent().apply {
        if (chatData.isNotEmpty() && !chatData.first().isStubItem()) {
            putExtra(ChatActivity.LAST_MESSAGE, toOldHistoryItem(chatData.first() as HistoryItem))
        }
        putParcelableArrayListExtra(ChatActivity.DISPATCHED_GIFTS, mDispatchedGifts)
        putExtra(SEND_MESSAGE, mIsSendMessage)
        putExtra(INTENT_USER_ID, mUser?.id ?: -1)
    }

    override fun unbind() {
        chatResult = null
        navigator = null
        overflowMenu = null
        activityFinisher = null
        mUpdateAdapterSubscription.safeUnsubscribe()
    }

    override fun release() {
        mDialogGetSubscription.get().safeUnsubscribe()
        arrayOf(mSendMessageSubscription, mMessageChangeSubscription, mUpdateHistorySubscription,
                mComplainSubscription, mHasPremiumSubscription, mDeleteSubscription,
                mResendSubscription).safeUnsubscribe()
    }

}
