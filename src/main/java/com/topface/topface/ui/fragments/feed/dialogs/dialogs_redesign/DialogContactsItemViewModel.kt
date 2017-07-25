package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Intent
import android.databinding.Observable.OnPropertyChangedCallback
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.view.View
import com.topface.topface.App
import com.topface.topface.requests.response.DialogContacts
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.dialogs.FeedPushHandler
import com.topface.topface.ui.fragments.feed.dialogs.IFeedPushHandlerListener
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.IOnListChangedCallbackBinded
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.toIntSafe
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscriber
import rx.Subscription

/**
 * Моедь итема хедера
 * Created by tiberal on 01.12.16.
 */
class DialogContactsItemViewModel(private val mContactsStubItem: DialogContactsStubItem, private val mApi: FeedApi, updateObservable: Observable<Bundle>, private val mFeedNavigator: IFeedNavigator)
    : ILifeCycle, IOnListChangedCallbackBinded, IFeedPushHandlerListener {

    val data = SingleObservableArrayList<Any>()
    val amount = ObservableField<String>()
    val commandVisibility = ObservableInt(View.VISIBLE)
    val counterVisibility = ObservableInt(View.INVISIBLE)
    private var mHasInitialData = mContactsStubItem.dialogContacts.items.isNotEmpty()
    private var mUpdateInProgress = false
    private var mUpdateSubscription: Subscription
    private var mMutualBandSubscription: Subscription? = null
    private val mPushHandler = FeedPushHandler(this)
    private var mContactsItemReadSubscription: Subscription? = null

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    companion object {
        private const val MAX_COUNTER: Byte = 100
        private const val OVER_ONE_HUNDRED = "99+"
    }

    init {
        mUpdateSubscription = updateObservable.subscribe(shortSubscription {
            if (mHasInitialData) {
                data.onCallbackBinded = this
                mHasInitialData = false
            } else {
                when {
                    data.observableList.isEmpty() -> loadMutual(to = null)
                    data.observableList.last() is DialogContactsItem -> {
                        val item = data.observableList.last() as DialogContactsItem
                        loadMutual(to = item.id)
                    }
                }
            }
        })
        amount.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: android.databinding.Observable?, p1: Int) {
                counterVisibility.set(if (with(obs as? ObservableField<String>) {
                    this?.get().isNullOrEmpty() || this?.get().toIntSafe() <= 0
                }) View.INVISIBLE else View.VISIBLE)
            }
        })
        // подписка на ивент о прочтении итема
        mContactsItemReadSubscription = mEventBus.getObservable(ContactsItemsReadEvent::class.java)
                .subscribe(shortSubscription { event ->
                    data.observableList.find { it is DialogContactsItem && it.id == event?.contactsItem?.id }
                            .to(mContactsStubItem.dialogContacts.items.find {
                                it.id == event?.contactsItem?.id
                            })
                            .run {
                                // 1-й - это элемент списка data.observableList
                                // 2-й - mContactsStubItem.dialogContacts.items
                                (first as? DialogContactsItem)?.unread = false
                                second?.unread = false
                                // если пользователя уже нет в списке, то не следует вызывать декремент счетчика
                                if (first != null || second != null) {
                                    decrementCounter()
                                }
                            }
                })
    }

    override fun onCallbackBinded() {
        data.addAll(mContactsStubItem.dialogContacts.items)
        amount.set(mContactsStubItem.dialogContacts.counter.toString())
    }

    private fun getAmount(counter: Byte) =
            if (counter == MAX_COUNTER) {
                OVER_ONE_HUNDRED
            } else {
                counter.toString()
            }

    fun loadMutual(from: Int? = null, to: Int? = null) {
        if (!mUpdateInProgress) {
            mUpdateInProgress = true
            mMutualBandSubscription = mApi.callMutualBandGetList(from = from, to = to).subscribe(object : Subscriber<DialogContacts>() {
                override fun onNext(data: DialogContacts?) {
                    mUpdateInProgress = false
                    data?.let {
                        if (this@DialogContactsItemViewModel.data.observableList.count() != 0 ||
                                it.items.isNotEmpty()) {
                            updateDialogContacts(it)
                            this@DialogContactsItemViewModel.data.addAll(it.items)
                            amount.set(getAmount(it.counter))
                            addFooterGoDatingItem(it.more)
                        } else {
                            contactsEmpty()
                        }
                    } ?: if (this@DialogContactsItemViewModel.data.observableList.count() == 0) {
                        contactsEmpty()
                    }

                    /* в контактах всегда будет минимум один элемент
                    * но надо проверить еще, что будет догрузка
                     */

                    mEventBus.setData(DialogContactsEvent(this@DialogContactsItemViewModel.data.observableList.count() > 1 || data?.more ?: false))
                }

                override fun onError(e: Throwable?) {
                    mUpdateInProgress = false
                    Utils.showErrorMessage()
                }

                override fun onCompleted() {
                    mUpdateInProgress = false
                    unsubscribe()
                }

            })
        }
    }

    private fun contactsEmpty() {
        commandVisibility.set(View.INVISIBLE)
        addEmptyContactsItem()
        amount.set(Utils.EMPTY)
    }

    /**
     * Обновляем инфу о контактах в data массиве адаптера. Чтобы не грузить все при следующем создании итема.
     */
    private fun updateDialogContacts(dialogContacts: DialogContacts) = with(mContactsStubItem.dialogContacts) {
        counter = dialogContacts.counter
        more = dialogContacts.more
        items.addAll(dialogContacts.items)
    }

    /**
     * Добавить итем, сообщеющий, что нет взаимных(в шапке)
     */
    private fun addEmptyContactsItem() = with(data.observableList) {
        clear()
        add(0, UForeverAloneStubItem())
    }

    /**
     * Добавить итем, сообщающий, что взаимные кончили, и надо идти знакомиться
     */
    private fun addFooterGoDatingItem(more: Boolean) {
        if (!more) {
            if (data.observableList.last() !is GoDatingContactsStubItem) {
                data.observableList.add(data.observableList.count(), GoDatingContactsStubItem())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            if (requestCode == ChatActivity.REQUEST_CHAT) {
                val userId = data.getIntExtra(ChatFragment.INTENT_USER_ID, -1)
                if (data.getBooleanExtra(ChatFragment.SEND_MESSAGE, false)) {
                    removeItemByUserId(userId)?.let {
                        if (it.unread) {
                            sendReadRequest(it).subscribe(shortSubscription {
                                if (it.completed) {
                                    decrementCounter()
                                }
                            })
                        }
                        showStubIfNeed()
                    }
                }
            }
        }
    }

    // проверяем List на длину, если там 1 итем и тот fake "иди знакомиться" -> показываем stub
    private fun showStubIfNeed() {
        val observableList = this@DialogContactsItemViewModel.data.observableList
        if (observableList.count() == 1 && observableList[0] is GoDatingContactsStubItem) {
            contactsEmpty()
        }
    }

    private fun removeItemByUserId(userId: Int): DialogContactsItem? {
        data.observableList.forEach {
            if (it is DialogContactsItem && it.user.id == userId) {
                data.observableList.remove(it)
                mContactsStubItem.dialogContacts.items.remove(it)
                mEventBus.setData(DialogContactsEvent(mContactsStubItem.dialogContacts.items.isNotEmpty()))
                showStubIfNeed()
                return it
            }
        }
        return null
    }

    private fun sendReadRequest(dialogContacts: DialogContactsItem) = if (dialogContacts.highrate) {
        mApi.callAdmirationRead(listOf(dialogContacts.id))
    } else {
        mApi.callMutualRead(listOf(dialogContacts.id))
    }

    fun release() {
        mUpdateSubscription.safeUnsubscribe()
        mMutualBandSubscription.safeUnsubscribe()
        mPushHandler.release()
        data.removeListener()
        mContactsItemReadSubscription.safeUnsubscribe()
    }

    override fun updateFeedMutual() = loadTop()

    override fun updateFeedAdmiration() = loadTop()

    override fun userAddToBlackList(userId: Int) {
        removeItemByUserId(userId)?.let {
            if (it.unread) {
                decrementCounter()
            }
        }
    }

    private fun decrementCounter() = amount.set(getAmount(mContactsStubItem.dialogContacts
            .apply { counter = if ((counter--) < 0) 0 else counter-- }
            .run { counter }))

    private fun loadTop() {
        if (data.observableList.isNotEmpty()) {
            val item = data.observableList.first()
            if (item is DialogContactsItem) {
                loadMutual(to = item.id)
            }
        }
    }

}