package com.topface.topface.ui.fragments.feed.enhanced.chat.chat_menu

import android.content.ClipData
import android.content.ClipboardManager
import android.databinding.ObservableInt
import android.os.Bundle
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.Api
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.api.responses.HistoryItem.Companion.FRIEND_GIFT
import com.topface.topface.api.responses.HistoryItem.Companion.USER_GIFT
import com.topface.topface.api.responses.HistoryItem.Companion.USER_MESSAGE
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatComplainEvent
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.showLongToast
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription

class ChatPopupMenuViewModel(arguments: Bundle,
                             private var mIDialogCloser: IDialogCloser?,
                             private val mClipboardManager: ClipboardManager,
                             private val mApi: Api) : ILifeCycle {

    private var mChatPopupSubscription: Subscription? = null

    private val mEventBus by lazy { App.getAppComponent().eventBus() }

    val mItemId = arguments.getInt(ChatPopupMenu.CHAT_ITEM_ID)

    val mItem: HistoryItem = arguments.getParcelable(ChatPopupMenu.CHAT_ITEM)

    val itemType = mItem.getItemType()

    val complainItemVisibility = ObservableInt(if (itemType == USER_MESSAGE || itemType == USER_GIFT) View.GONE else View.VISIBLE)

    val copyItemVisibility = ObservableInt(if (itemType == USER_GIFT || itemType == FRIEND_GIFT) View.GONE else View.VISIBLE)

    fun copyMessage() {
        mClipboardManager.primaryClip = ClipData.newPlainText(Utils.EMPTY, mItem.text)
        R.string.general_msg_copied.showLongToast()
        mIDialogCloser?.closeIt()
    }

    fun complain() {
        mEventBus.setData(ChatComplainEvent(mItemId))
        mIDialogCloser?.closeIt()
    }

    fun deleteMessage() {
        mApi.execDeleteMessage(mItem)
        mIDialogCloser?.closeIt()
    }

    fun release() {
        mChatPopupSubscription.safeUnsubscribe()
        mIDialogCloser = null
    }
}