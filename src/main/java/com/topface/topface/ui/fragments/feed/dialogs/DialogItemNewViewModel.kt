package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.ObservableField
import android.graphics.Typeface
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.Profile
import com.topface.topface.data.User
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.glide_utils.GlideTransformationType

/**
 * Created by mbulgakov on 28.11.16. НОВЫЙ ВАРИАНТ ИТЕМА
 */
class DialogItemNewViewModel(val item: FeedDialog, val navigator: IFeedNavigator) {

    val context = App.getContext()

    val userPhoto = ObservableField(item.user.photo)
    val type = ObservableField(if (item.user.online) GlideTransformationType.ONLINE_TYPE else GlideTransformationType.CROP_CIRCLE_TYPE)
    val placeholderRes = ObservableField(if (item.user.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small)
    val counterVisibility: ObservableField<Int> = ObservableField(if (item.unread) View.VISIBLE else View.GONE)
    val name: ObservableField<String> = ObservableField(item.user.firstName)
    val dialogTextColor: ObservableField<Int> = ObservableField(if (item.unread) context.resources.getColor(R.color.message_unread) else context.resources.getColor(R.color.message_was_read))
    val dialogMessageStyle: ObservableField<Int> = ObservableField(if (item.unread) Typeface.BOLD else Typeface.NORMAL)
    val dialogMessageIcon: ObservableField<Int> = ObservableField(prepareMessageIcon())
    val dialogTime: ObservableField<String> = ObservableField(item.createdRelative)
    val text: ObservableField<String> = ObservableField(prepareDialogText())

    private fun getStubResourсe() = if (item.user.sex == Profile.BOY) R.drawable.feed_banned_male_avatar else R.drawable.feed_banned_female_avatar

    private fun prepareMessageIcon() =
            when (item.type) {
                FeedDialog.DEFAULT, FeedDialog.MESSAGE, FeedDialog.MESSAGE_WISH,
                FeedDialog.MESSAGE_SEXUALITY, FeedDialog.MESSAGE_WINK,
                FeedDialog.RATE, FeedDialog.PROMOTION,
                FeedDialog.PHOTO -> if (item.target == FeedDialog.OUTPUT_USER_MESSAGE) R.drawable.arrow_dialogues else 0
                FeedDialog.LIKE -> if (item.target != FeedDialog.INPUT_FRIEND_MESSAGE) R.drawable.arrow_dialogues else 0
                FeedDialog.SYMPHATHY -> R.drawable.dialogues_sympathy
                FeedDialog.GIFT -> R.drawable.dialogues_gift
                else -> if (item.target == FeedDialog.OUTPUT_USER_MESSAGE) R.drawable.arrow_dialogues else 0
            }

    private fun prepareDialogText() =
            when {
                item.user.deleted -> context.getString(R.string.user_is_deleted)
                item.user.banned -> context.getString(R.string.user_is_banned)
                else -> pripareDialogsByType()
            }

    private fun pripareDialogsByType() =
            when (item.type) {
                FeedDialog.LIKE -> if (item.target == FeedDialog.INPUT_FRIEND_MESSAGE) context.getString(R.string.chat_like_in) else context.getString(R.string.chat_like_out)
                FeedDialog.SYMPHATHY -> context.getString(R.string.mutual_sympathy)
                FeedDialog.GIFT -> if (item.target == FeedDialog.INPUT_FRIEND_MESSAGE) context.getString(R.string.chat_gift_in) else context.getString(R.string.chat_gift_out)
                else -> Utils.EMPTY
            }

    fun onClick() = navigator.showChat(item)

    fun onLongClick() {
        // открытие опшнМеню с удалением и черным списком
    }

}