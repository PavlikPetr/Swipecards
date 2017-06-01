package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.widget.Toast
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Photo
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getPlaceholderRes
import com.topface.topface.utils.extensions.showLongToast

class ChatToolbarAvatarModel(private val user: FeedUser?, private val navigator: IFeedNavigator) {

    var photo = ObservableField(user?.photo)
    val placeholderRes = ObservableInt(user.getPlaceholderRes(true))

    init {
        if (user != null && (user.isEmpty || user.banned || user.deleted || user.photo?.isEmpty ?: true)) {
            photo.set(Photo.createFakePhoto())
        }
    }

    fun goToProfile() = user?.let {
        if (it.deleted || it.banned) {
            R.string.user_deleted_or_banned.showLongToast()
        } else {
            navigator.showProfile(it, "chat_fragment")
        }
    }
}