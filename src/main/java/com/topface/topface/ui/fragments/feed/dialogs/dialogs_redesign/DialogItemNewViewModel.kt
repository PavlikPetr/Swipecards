package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.ObservableField
import android.graphics.Typeface
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.Profile
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
    val counterVisibility: ObservableField<Int> = ObservableField(if (item.unread) View.VISIBLE else View.GONE)
    val name: ObservableField<String> = ObservableField(item.user.firstName)
    val dialogTextColor: ObservableField<Int> = ObservableField(if (item.unread) context.resources.getColor(R.color.message_unread) else context.resources.getColor(R.color.message_was_read))
    val dialogMessageStyle: ObservableField<Int> = ObservableField(if (item.unread) Typeface.BOLD else Typeface.NORMAL)
    val dialogMessageIcon: ObservableField<Int> = ObservableField(if (item.target == FeedDialog.OUTPUT_USER_MESSAGE) R.drawable.arrow_dialogues else 0)
    val dialogTime: ObservableField<String> = ObservableField(item.createdRelative)
    val text: ObservableField<String> = ObservableField(prepareDialogText())

    private fun getStubResourсe() = if (item.user.sex == Profile.BOY) R.drawable.feed_banned_male_avatar else R.drawable.feed_banned_female_avatar

    private fun prepareDialogText() =
            when {
                item.user.deleted -> context.getString(R.string.user_is_deleted)
                item.user.banned -> context.getString(R.string.user_is_banned)
                else -> Utils.EMPTY
            }


    fun onClick() = navigator.showChat(item)

    fun onLongClick() {
        // открытие опшнМеню с удалением и черным списком
    }

}