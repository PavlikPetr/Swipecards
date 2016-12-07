package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Context
import android.content.Intent
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.view.View
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.requests.*
import com.topface.topface.requests.response.DialogContacts
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.IOnListChangedCallbackBinded
import com.topface.topface.utils.databinding.SingleObservableArrayList
import rx.Observable

/**
 * Моедь итема хедера
 * Created by tiberal on 01.12.16.
 */
class DialogContactsItemViewModel(private val mContext: Context, private val mContactsStubItem: DialogContactsStubItem, updateObservable: Observable<Bundle>)
    : ILifeCycle, IOnListChangedCallbackBinded {

    val data = SingleObservableArrayList<Any>()
    val amount = ObservableField<String>()
    val commandVisibility = ObservableInt(View.VISIBLE)
    val counterVisibility = ObservableInt(View.INVISIBLE)
    private var mHasInitialData = mContactsStubItem.dialogContacts.items.isNotEmpty()
    private var mUpdateInProgress = false

    private companion object {
        const val MAX_COUNTER: Byte = 100
        const val OVER_ONE_HUNDRED = "99+"
    }

    init {
        updateObservable.subscribe {
            if (mHasInitialData) {
                data.onCallbackBinded = this
                mHasInitialData = false
            } else {
                testRequest(if (data.observableList.isEmpty()) null else (data.observableList.last() as DialogContactsItem).id)
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

    fun testRequest(to: Int?) {
        if (!mUpdateInProgress) {
            mUpdateInProgress = true
            MutualBandGetListRequest(App.getContext(), 10, to = to).callback(object : DataApiHandler<DialogContacts>() {
                override fun success(data: DialogContacts?, response: IApiResponse?) {
                    mUpdateInProgress = false
                    data?.let {
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

                override fun parseResponse(response: ApiResponse?) = response?.jsonResult?.toString()?.run {
                    JsonUtils.fromJson<DialogContacts>(this, DialogContacts::class.java)
                }

                override fun fail(codeError: Int, response: IApiResponse?) {
                    mUpdateInProgress = false
                    Utils.showErrorMessage()
                }

            }).exec()
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
                if (removeItemUserId(userId)) {
                    sendReadRequest(userId)
                }

            }
        }
    }

    private fun removeItemUserId(userId: Int): Boolean {
        data.observableList.forEach {
            if (it is DialogContactsItem && it.user.id == userId) {
                data.observableList.remove(it)
                return true
            }
        }
        return false
    }

    private fun sendReadRequest(userId: Int) {
        if (true) {
            MutualReadRequest(mContext, userId).exec()
        } else {
            ReadLikeRequest(mContext, userId).exec()
        }
    }

    fun release() {
        data.removeListener()
    }

}