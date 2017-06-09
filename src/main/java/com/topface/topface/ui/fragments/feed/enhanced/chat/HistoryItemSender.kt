package com.topface.topface.ui.fragments.feed.enhanced.chat

import com.topface.framework.utils.Debug
import com.topface.topface.api.IApi
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.utils.rx.shortSubscription
import rx.subscriptions.CompositeSubscription

/**
 * Sends and resends HistoryItems
 */
object HistoryItemSender {
    fun send(sendSubscription: CompositeSubscription, api: IApi, item: HistoryItem, uId: Int,
             onInit:() -> Unit, onSuccess: (HistoryItem) -> Unit) {
        sendSubscription.add(api.callSendMessage(uId, item.text)
                .doOnSubscribe {
                    onInit()
                    item.isErrorVisible.set(false)
                }
                .subscribe(shortSubscription({
                    Debug.log("FUCKING_CHAT send fail")
                    item.isErrorVisible.set(true)
                    item.isRetrierVisible.set(true)
                    item.isSending.set(false)
                }, {
                    onSuccess(it)
                    item.isRetrierVisible.set(false)
                    item.isErrorVisible.set(false)
                    item.isSending.set(false)
                })))
    }
}

data class SendHistoryItemEvent(val item: HistoryItem)