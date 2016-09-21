package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.FeedItemDialogBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.Utils

/**
 * Моделька для тема диалога. С счетчиками сообщений и иконками в превьюхах диалогов
 * Created by tiberal on 18.09.16.
 */
class DialogsItemViewModel(binding: FeedItemDialogBinding, item: FeedDialog, navigator: IFeedNavigator, isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedItemDialogBinding, FeedDialog>(binding, item, navigator, isActionModeEnabled) {

    private companion object {
        val MIN_MSG_AMOUNT_TO_SHOW = 2
        val MAX_MESSAGES_IN_CHAT_ITEM = 99
        val OVER_99_MESSAGES = "99+"
    }

    val counterVisibility = ObservableInt(View.GONE)
    val dialogTextColor: Int
        get() {
            return if (item.unread && App.get().options.hidePreviewDialog) {
                context.resources.getColor(R.color.hidden_dialog_preview_text_color)
            } else {
                context.resources.getColor(R.color.list_text_gray)
            }
        }
    val dialogMessageCounter: String
        get() = prepareMessageCounter()
    val dialogMessageIcon: Int
        get() = prepareDialogMessageIcon()
    val dialogTime: String
        get() = item.createdRelative
    override val text: String
        get() = prepareDialogText()

    fun prepareMessageCounter(): String {
        val amount = item.unreadCounter
        var counter = Utils.EMPTY
        if (amount >= MIN_MSG_AMOUNT_TO_SHOW) {
            counter = if (amount > MAX_MESSAGES_IN_CHAT_ITEM)
                OVER_99_MESSAGES
            else
                Integer.toString(amount)
            counterVisibility.set(View.VISIBLE)
        } else {
            counterVisibility.set(View.GONE)
        }
        return counter
    }


    private fun prepareDialogMessageIcon(): Int {
        var image = 0
        when (item.type) {
            FeedDialog.DEFAULT, FeedDialog.MESSAGE, FeedDialog.MESSAGE_WISH,
            FeedDialog.MESSAGE_SEXUALITY, FeedDialog.MESSAGE_WINK,
            FeedDialog.RATE, FeedDialog.PROMOTION,
            FeedDialog.PHOTO -> image = if (item.target == FeedDialog.OUTPUT_USER_MESSAGE)
                R.drawable.ico_outbox
            else
                0
            FeedDialog.LIKE -> if (item.target == FeedDialog.INPUT_FRIEND_MESSAGE) {
            } else {
                image = R.drawable.ico_outbox
            }
            FeedDialog.SYMPHATHY -> {
                image = R.drawable.ic_mutuality_msg_list
            }
            FeedDialog.GIFT -> {
                image = R.drawable.ico_gift
            }
        }
        //Если иконка или текст пустые, то ставим данные по умолчанию
        return if (image == 0 && item.target == FeedDialog.OUTPUT_USER_MESSAGE)
            R.drawable.ico_outbox
        else
            image

    }

    private fun prepareDialogText(): String {
        var text: String = Utils.EMPTY
        if (item.user.deleted) {
            text = context.getString(R.string.user_is_deleted)
        } else if (item.user.banned) {
            text = context.getString(R.string.user_is_banned)
        } else {
            when (item.type) {
                FeedDialog.LIKE -> if (item.target == FeedDialog.INPUT_FRIEND_MESSAGE) {
                    text = context.getString(R.string.chat_like_in)
                } else {
                    text = context.getString(R.string.chat_like_out)
                }
                FeedDialog.SYMPHATHY -> {
                    text = context.getString(R.string.mutual_sympathy)
                }
                FeedDialog.GIFT -> {
                    text = if (item.target == FeedDialog.INPUT_FRIEND_MESSAGE)
                        context.getString(R.string.chat_gift_in)
                    else
                        context.getString(R.string.chat_gift_out)
                }
                FeedDialog.MESSAGE_AUTO_REPLY -> text = Utils.EMPTY
            }
        }
        if (item.unread && App.get().options.hidePreviewDialog) {
            text = Utils.getQuantityString(R.plurals.notification_many_messages,
                    item.unreadCounter, item.unreadCounter)
        }
        return if (text.equals(Utils.EMPTY)) item.text else text
    }

}