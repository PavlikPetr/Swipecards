package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Context
import android.content.Intent
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.requests.*
import com.topface.topface.requests.response.DialogContacts
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.SingleObservableArrayList

/**
 * Моедь итема хедера
 * Created by tiberal on 01.12.16.
 */
class DialogContactsItemViewModel(private val mContext: Context) : ILifeCycle {

    val data = SingleObservableArrayList<Any>()
    val amount = ObservableField<String>()
    val commandVisibility = ObservableInt(View.VISIBLE)
    val counterVisibility = ObservableInt(View.INVISIBLE)

    private companion object {
        const val MAX_COUNTER: Byte = 100
        const val OVER_ONE_HUNDRED = "99+"
    }

    init {
        testRequest()
    }

    private fun getAmount(counter: Byte) =
            if (counter == MAX_COUNTER) {
                OVER_ONE_HUNDRED
            } else {
                counter.toString()
            }

    fun testRequest() {
        MutualBandGetListRequest(App.getContext(), 10).callback(object : DataApiHandler<DialogContacts>() {
            override fun success(data: DialogContacts?, response: IApiResponse?) {
                data?.let {
                    if (it.items.isNotEmpty()) {
                        this@DialogContactsItemViewModel.data.addAll(data.items)
                        commandVisibility.set(if (it.items.isEmpty()) View.INVISIBLE else View.VISIBLE)
                        counterVisibility.set(if (it.counter > 0) View.VISIBLE else View.INVISIBLE)
                        amount.set(getAmount(it.counter))
                        addFooterItem(it.more)
                    } else {
                        addEmptyContactsItem()
                    }
                }
                Debug.log("")
            }

            override fun parseResponse(response: ApiResponse?): DialogContacts? {
                val result = response?.jsonResult?.toString()?.run {
                    JsonUtils.fromJson<DialogContacts>(this, DialogContacts::class.java)
                }
                return result
            }

            override fun fail(codeError: Int, response: IApiResponse?) {
                Debug.log("")
            }

        }).exec()
    }

    private fun addEmptyContactsItem() = with(data.observableList) {
        clear()
        add(0, UForeverAloneStubItem())
    }

    private fun addFooterItem(more: Boolean) {
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