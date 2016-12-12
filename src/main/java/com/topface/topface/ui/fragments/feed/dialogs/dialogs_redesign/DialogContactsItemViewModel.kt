package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Context
import android.content.Intent
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.view.View
import com.topface.topface.App
import com.topface.topface.requests.MutualReadRequest
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.requests.response.DialogContacts
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.state.EventBus
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.dialogs.FeedPushHandler
import com.topface.topface.ui.fragments.feed.dialogs.IFeedPushHandlerListener
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.IOnListChangedCallbackBinded
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.safeUnsubscribe
import rx.Observable
import rx.Subscriber
import rx.Subscription
import javax.inject.Inject

/**
 * Моедь итема хедера
 * Created by tiberal on 01.12.16.
 */
class DialogContactsItemViewModel(private val mContext: Context, private val mContactsStubItem: DialogContactsStubItem, private val mApi: FeedApi, updateObservable: Observable<Bundle>)
    : ILifeCycle, IOnListChangedCallbackBinded, IFeedPushHandlerListener {

    val data = SingleObservableArrayList<Any>()
    val amount = ObservableField<String>()
    val commandVisibility = ObservableInt(View.VISIBLE)
    val counterVisibility = ObservableInt(View.INVISIBLE)
    private var mHasInitialData = mContactsStubItem.dialogContacts.items.isNotEmpty()
    private var mUpdateInProgress = false
    private var mUpdateSubscription: Subscription
    private var mMutualBandSubscription: Subscription? = null
    private val mPushHandler = FeedPushHandler(this, mContext)

    @Inject lateinit var mEventBus: EventBus

    companion object {
        private const val MAX_COUNTER: Byte = 100
        private const val OVER_ONE_HUNDRED = "99+"
    }

    init {
        App.get().inject(this)
        mUpdateSubscription = updateObservable.subscribe {
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
        }
    }

    override fun onCallbackBinded() = data.addAll(mContactsStubItem.dialogContacts.items)

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
                        mEventBus.setData(DialogContactsEvent(it.items.isNotEmpty()))
                        if (it.items.isNotEmpty()) {
                            updateDialogContacts(it)
                            this@DialogContactsItemViewModel.data.addAll(data.items)
                            amount.set(getAmount(it.counter))
                            addFooterGoDatingItem(it.more)
                        } else {
                            commandVisibility.set(View.INVISIBLE)
                            addEmptyContactsItem()
                        }
                    }
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
            data.observableList.add(data.observableList.count(), GoDatingContactsStubItem())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            if (requestCode == ChatActivity.REQUEST_CHAT
                    && data.getBooleanExtra(ChatFragment.SEND_MESSAGE, false)) {
                val userId = data.getIntExtra(ChatFragment.INTENT_USER_ID, -1)
                if (removeItemByUserId(userId)) {
                    sendReadRequest(userId)
                }

            }
        }
    }

    private fun removeItemByUserId(userId: Int): Boolean {
        data.observableList.forEach {
            if (it is DialogContactsItem && it.user.id == userId) {
                data.observableList.remove(it)
                return true
            }
        }
        return false
    }

    private fun sendReadRequest(userId: Int) = if (true) {
        MutualReadRequest(mContext, userId).exec()
    } else {
        ReadLikeRequest(mContext, userId).exec()
    }

    fun release() {
        mUpdateSubscription.safeUnsubscribe()
        mMutualBandSubscription.safeUnsubscribe()
        mPushHandler.release()
        data.removeListener()
    }

    override fun updateFeedMutual() = loadTop()

    override fun updateFeedAdmiration() = loadTop()

    private fun loadTop() {
        val item = data.observableList.first()
        if (data.observableList.isNotEmpty() && item is DialogContactsItem) {
            loadMutual(from = item.id)
        }
    }

}