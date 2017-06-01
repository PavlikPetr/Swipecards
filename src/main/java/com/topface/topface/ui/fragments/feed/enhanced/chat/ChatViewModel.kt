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
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Gift
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.ComplainsActivity
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.PurchasesActivity
import com.topface.topface.ui.fragments.feed.FeedFragment
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
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

/**                         Условия показа и типы блокировок. Инфа актуальна с 31.05.2017
 *
 *                  Блокировка "сообщением о взаимной симпатии"
 *
 * Условия: В истории сообщений приходит сообщение с типом(historyItem.type) MUTUAL_SYMPATHY. Вне зависимости вип или Невип
 * Что показываем: com.topface.topface.ui.fragments.feed.enhanced.chat.stubs.MutualStubChatViewModel
 * Блокиировка экрана чата: Пользователь может отправить сообщение или подарок, после отправки заглушку убираем
 *
 *                  Блокировка "У вас есть взаимная симпатия, напишите первым"
 *
 * Условия: История сообщений пуста. Пользователь заходит в чат с юзером, с которым у него ЕСТЬ взаимная симпатия.
 *          Вне зависимости вип или НЕвип. Переход в чат может быть с любого экрана(симпатии, гости , знакомства и пр.)
 * Что показываем: com.topface.topface.ui.fragments.feed.enhanced.chat.stubs.MutualStubChatViewModel
 * Блокиировка экрана чата: Пользователь может отправить сообщение или подарок, после отправки заглушку убираем
 *
 *                  Блокировка "У вас нет взаимной симпатии. купите вип, чтобы писать без взаимной симпатии"
 *
 * Условия: История сообщений пуста. Пользователь заходит в чат с юзером, с которым у него НЕТ взаимной симпатии.
 *          Пользователь НЕвип. Переход в чат может быть с любого экрана(симпатии, гости , знакомства и пр.)
 * Что показываем: com.topface.topface.ui.fragments.feed.enhanced.chat.stubs.NotVipAndSympViewModel
 * Блокировки экрана чата: Пользователь НЕ может отправить сообщение или подарок(кнопки disable, поле ввода текста также НЕактивно).
 *                          Единственное действие переход по кнопке для покупки випа. В случае покупки випа удаляем заглушку.
 *
 *                  Блокировка LOCK_CHAT_STUB (В рамках эксперимента 57-2 приходит сообщения из базы бомб от популярного пользователя)
 *
 * Условия: В истории сообщений приходит сообщение с типом(historyItem.type) LOCK_CHAT. Пользователь НЕвип.
 *          В диалогах это выглядит как пустое сообщение, при переходе показываем.
 * Что показываем: com.topface.topface.ui.fragments.feed.enhanced.chat.stubs.BuyVipStubViewModel
 * Блокировка экрана чата: Пользователь НЕ может отправить сообщение или подарок(кнопки disable, поле ввода текста также НЕактивно).
 *                          Единственное действие переход по кнопке для покупки випа. В случае покупки випа удаляем заглушку.
 *
 *                  Блокировка LOCK_MESSAGE_SEND (В рамках эксперимента 57-2 приходит сообщения из базы бомб от популярного пользователя)
 *
 * Условия: В истории сообщений приходит сообщение с типом(historyItem.type) LOCK_MESSAGE_SEND. Пользователь НЕвип.
 *          Показываем пользователю при нажатии на отправку сообщения.
 * Что показываем: com.topface.topface.ui.fragments.feed.enhanced.chat.message_36_dialog.СhatMessage36DialogViewModel
 * Блокировка экрана чата: При попытке отправить сообщение показываем попап. Единственное действие - переход на покупку вип. Купил - убираем блокировку
 *
 *                  Отсутствие блокировоk
 *
 *  Пользователь может кому-то написать первым только в случае:
 *   - не выполняются условия для показа блокировок
 *   - у него есть вип
 *   - у него есть взаимная симпатия с юзером, которому он пишет
 *  Пользователь может ответить в случае:
 *   - не выполняются условия для показа блокировok
 *   - если ему пришло сообщение
 *
 */

class ChatViewModel(private val mContext: Context, private val mApi: Api, private val mEventBus: EventBus, private val mState: TopfaceAppState) : BaseViewModel() {

    companion object {
        private const val DEFAULT_CHAT_UPDATE_PERIOD = 10000
        private const val EMPTY = ""
        private const val MUTUAL_SYMPATHY = 7
        private const val LOCK_CHAT = 35
        private const val LOCK_MESSAGE_SEND = 36
        private const val SEND_MESSAGE = "send_message"
        private const val INTENT_USER_ID = "user_id"
        const val SERVER_TIME_CORRECTION = 1000L
        const val LAST_ITEM_ID = "last id"

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

    val isComplainVisible = ObservableInt(View.VISIBLE)
    val isChatVisible = ObservableInt(View.VISIBLE)
    val isSendButtonEnable = ObservableBoolean(false)
    val isSendGiftEnable = ObservableBoolean(true)
    val isEditTextEnable = ObservableBoolean(true)
    val message = RxObservableField<String>(Utils.EMPTY)
    val chatData = ChatData()
    var updateObservable: Observable<Bundle>? = null
    private var mDialogGetSubscription = AtomicReference<Subscription>()
    private var mSendMessageSubscription: CompositeSubscription = CompositeSubscription()
    private var mMessageChangeSubscription: Subscription? = null
    private var mUpdateHistorySubscription: Subscription? = null
    private var mComplainSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null

    private var mUser: FeedUser? = null

    /**
     * Флаг говорящий о том, что етсть итемы с id = 0, и их нужно удалить и заменить на нормальные
     * при следующем update
     */
    private var mHasStubItems = false
    private var mIsPremium = false
    private var mIsNeedShowAddPhoto = true
    var blockChatType: Int = NO_BLOCK

    /**
     * Коллекция отправленных из чатика подарочков. Нужны, чтобы обновльты изтем со списком
     * подарочков юзера в дейтинге. Как только дейтинг будет переделан на новый скраффи, там сразу
     * можно будет лофить ивент об успешном отправлении подарочка, и сразу его добавлять
     */
    private var mDispatchedGifts: ArrayList<Gift> = ArrayList()
    private var mIsSendMessage = false

    override fun bind() {
        mUser = args?.getParcelable(ChatIntentCreator.WHOLE_USER)
        takePhotoIfNeed()
        val adapterUpdateObservable = updateObservable
                ?.distinct { it.getInt(LAST_ITEM_ID) }
                ?.map { createUpdateObject(mUser?.id ?: -1) }
                ?: Observable.empty()

        mMessageChangeSubscription = message.asRx.subscribe(shortSubscription {
            isSendButtonEnable.set(it.isNotBlank())
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
            removeByPredicate { it.id == 0 || it.type == MUTUAL_SYMPATHY || it.type == LOCK_CHAT }
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

    /**
     * Обновление по эмитам гцм, птр, таймера
     */
    private fun update(updateContainer: Triple<Int, String?, String?>) {

        val addToStart = updateContainer.second != null
        mDialogGetSubscription.set(mApi.callDialogGet(updateContainer.first, updateContainer.second, updateContainer.third)
                .subscribe(shortSubscription({
                    mDialogGetSubscription.get()?.unsubscribe()
                }, {
                    chatResult?.setResult(createResultIntent())
                    setStubsIfNeed(it)
                    setBlockSettings()
                    if (it?.items?.isNotEmpty() ?: false) {
                        val items = ArrayList<HistoryItem>()
                        it.items.forEach {
                            when (it.type) {
                                LOCK_CHAT, MUTUAL_SYMPATHY -> mHasStubItems = true
                            }
                            if (blockChatType == NO_BLOCK || blockChatType == LOCK_MESSAGE_FOR_SEND) {
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

    private fun setStubsIfNeed(history: History) {
        var stub: Any? = null
        if (history.items.isEmpty() && chatData.isEmpty()) {
            if (history.mutualTime != 0) {
                blockChatType = MUTUAL_SYMPATHY_STUB
                stub = MutualStub()
            } else if (!mIsPremium) {
                blockChatType = NO_MUTUAL_NO_VIP_STUB
                stub = NotMutualBuyVipStub()
            }
        } else isEditTextEnable.set(true)

        if (history.items.isNotEmpty() && chatData.isEmpty() && !mIsPremium && !mHasStubItems) {
            history.items.forEach {
                stub = when (it.type) {
                    MUTUAL_SYMPATHY -> {
                        blockChatType = MUTUAL_SYMPATHY_STUB
                        MutualStub()
                    }
                    LOCK_CHAT -> {
                        blockChatType = LOCK_CHAT_STUB
                        BuyVipStub()
                    }
                    LOCK_MESSAGE_SEND -> {
                        blockChatType = LOCK_MESSAGE_FOR_SEND
                        null
                    }
                    else -> {
                        blockChatType = NO_BLOCK
                        null
                    }
                }
            }
        }
        stub?.let { chatData.add(stub) }
    }

    private fun setBlockSettings() {
        when (blockChatType) {
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
        if (blockChatType != LOCK_MESSAGE_FOR_SEND) {
            mSendMessageSubscription.add(mApi.callSendMessage(it.id, message)
                    .doOnSubscribe {
                        mHasStubItems = true
                        mIsSendMessage = true
                        if (blockChatType == MUTUAL_SYMPATHY_STUB) {
                            chatData.clear()
                            blockChatType = NO_BLOCK
                        }
                        chatData.add(0, wrapHistoryItem(HistoryItem(text = message,
                                created = System.currentTimeMillis() / SERVER_TIME_CORRECTION)))
                        this.message.set(EMPTY)
                    }
                    .subscribe(shortSubscription({
                        Debug.log("FUCKING_CHAT send fail")
                    }, {
                        chatResult?.setResult(createResultIntent())
                    })))
        } else {
            mUser?.let { navigator?.showUserIsTooPopularLock(it) }
        }
    }

    fun onGift() = mUser?.let {
        if (blockChatType != LOCK_MESSAGE_FOR_SEND) {
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
            PurchasesActivity.INTENT_BUY_VIP -> {
                chatData.clear()
                update(createUpdateObject(mUser?.id ?: -1))
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
        com.topface.topface.data.History(JSONObject(it.toJson())).apply { user = mUser }
    }

    internal fun createResultIntent() = Intent().apply {
        if (chatData.isNotEmpty()) {
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
    }

    override fun release() {
        mDialogGetSubscription.get().safeUnsubscribe()
        arrayOf(mSendMessageSubscription, mMessageChangeSubscription, mUpdateHistorySubscription,
                mComplainSubscription, mDeleteSubscription).safeUnsubscribe()
    }

}
