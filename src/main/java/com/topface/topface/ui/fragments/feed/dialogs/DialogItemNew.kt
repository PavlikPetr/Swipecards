package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.ObservableField
import android.graphics.Typeface
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.Profile
import com.topface.topface.databinding.FeedItemDialogNewBinding
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.AvatarHolder
import com.topface.topface.utils.Utils

/**
 * Created by mbulgakov on 28.11.16. НОВЫЙ ВАРИАНТ ИТЕМА   TODO Выпилить параметры от BaseFeedItemViewModel!!!!! они оставлены для того, чтобы не говнять старый DialogsAdapter и смочь проверить
 */
class DialogItemNew(binding: FeedItemDialogNewBinding,
                    val item: FeedDialog,
                    val navigator: IFeedNavigator,
                    isActionModeEnabled: () -> Boolean) {

    var avatarHolder: AvatarHolder? = null   // todo нужно для аватарок. Когда Серега допилит аватарки, то наверно будет иначе, главное не забыть, что это еБиндинг и что переменная имеется в разметке

    init {
        avatarHolder = AvatarHolder(item.user.photo, getStubResourсe())
    }

    val context = App.getContext()

    val counterVisibility: ObservableField<Int> = ObservableField(if (item.unread) View.VISIBLE else View.GONE)

    val name: ObservableField<String> = ObservableField(item.user.firstName)

    val dialogTextColor: ObservableField<Int> = ObservableField(if (item.unread) context.resources.getColor(R.color.message_unread) else context.resources.getColor(R.color.message_was_read))

    val dialogMessageStyle: ObservableField<Int> = ObservableField(if (item.unread) Typeface.BOLD else Typeface.NORMAL)

    val dialogMessageIcon: ObservableField<Int> = ObservableField(if (item.target == FeedDialog.OUTPUT_USER_MESSAGE) R.drawable.arrow_dialogues else 0)

    val dialogTime: ObservableField<String> = ObservableField(item.createdRelative)

    val text: ObservableField<String> = ObservableField(prepareDialogText())

    private fun getStubResourсe() = if (item.user.sex == Profile.BOY) R.drawable.feed_banned_male_avatar else R.drawable.feed_banned_female_avatar

    private fun prepareDialogText(): String {
        var text: String = Utils.EMPTY
        if (item.user.deleted) {
            text = context.getString(R.string.user_is_deleted)
        } else if (item.user.banned) {
            text = context.getString(R.string.user_is_banned)
        } else text = item.text
        return text
    }

    fun onClick(view: View) {
        navigator.showChat(item)
    }

}