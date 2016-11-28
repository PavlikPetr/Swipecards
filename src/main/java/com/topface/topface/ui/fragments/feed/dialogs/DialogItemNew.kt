package com.topface.topface.ui.fragments.feed.dialogs

import android.graphics.Typeface
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.FeedItemDialogNewBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.Utils

/**
 * Created by mbulgakov on 28.11.16. НОВЫЙ ВАРИАНТ ИТЕМА   TODO Выплить BaseFeedItemViewModel!!!!!!!!!!!!      посмотреть, если что-то нужно... а если нет, то к хуям его
 */
class DialogItemNew(binding: FeedItemDialogNewBinding,
                    item: FeedDialog,
                    navigator: IFeedNavigator,
                    isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedItemDialogNewBinding, FeedDialog>(binding, item, navigator, isActionModeEnabled) {


    override val feed_type: String
        get() = "Dialog"

    val counterVisibility: Int
        get() {
            return if (item.unread) View.VISIBLE else View.GONE
        }

    val name: String
        get() {
            return item.user.firstName
        }

    val dialogTextColor: Int
        get() {
            return if (item.unread) {
                context.resources.getColor(R.color.message_unread)
            } else {
                context.resources.getColor(R.color.message_was_read)
            }
        }

    val dialogMessageStyle: Int
        get() {
            return if (item.unread) Typeface.BOLD else Typeface.NORMAL
        }

    //    val textHuexst: String
//        get() = if (item.user.firstName == "Аня") "Дорый день! Идите нахуй!" else "Добрый вечер!"
//todo проверочка такая
    val dialogMessageIcon: Int
        get() = if (item.target == FeedDialog.OUTPUT_USER_MESSAGE) R.drawable.arrow_dialogues else 0

    val dialogTime: String
        get() = item.createdRelative

    override val text: String
        get() = prepareDialogText()


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