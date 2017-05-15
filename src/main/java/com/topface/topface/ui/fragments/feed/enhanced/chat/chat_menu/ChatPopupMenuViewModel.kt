package com.topface.topface.ui.fragments.feed.enhanced.chat.chat_menu

import android.content.ClipData
import android.content.ClipboardManager
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.Api
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatComplainEvent
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatDeleteEvent
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.showLongToast
import com.topface.topface.utils.rx.shortSubscription
import rx.subscriptions.CompositeSubscription

class ChatPopupMenuViewModel(private val mItem: HistoryItem, private val mItemPosition: Int,
                             private var mIDialogCloser: IDialogCloser?, private val mApi: Api,
                             private val mClipboardManager: ClipboardManager) : ILifeCycle {

    private val mChatPopupSubscription = CompositeSubscription()

    private val mEventBus by lazy { App.getAppComponent().eventBus() }

    fun copyMessage() {
        mClipboardManager.primaryClip = ClipData.newPlainText(Utils.EMPTY, mItem.text)
        R.string.general_msg_copied.showLongToast()
        mIDialogCloser?.closeIt()
    }

    fun complain() {
        mEventBus.setData(ChatComplainEvent(mItemPosition))
        mIDialogCloser?.closeIt()
    }

    fun deleteMessage() {
        mChatPopupSubscription.add(mApi.callDeleteMessage(mItem).subscribe(shortSubscription {
            if (it.completed) {
                mEventBus.setData(ChatDeleteEvent(mItemPosition))
            }
        }
        ))
        mIDialogCloser?.closeIt()
    }

    fun release() {
        mChatPopupSubscription.clear()
        mIDialogCloser = null
    }
}