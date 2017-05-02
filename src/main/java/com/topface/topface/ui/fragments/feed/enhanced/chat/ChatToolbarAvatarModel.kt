package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Photo
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getPlaceholderRes

class ChatToolbarAvatarModel(private val user: FeedUser?, private val navigator: IFeedNavigator) {

    var photo = ObservableField<Photo>(user?.photo)
    val placeholderRes = ObservableInt(user.getPlaceholderRes())

    fun goToProfile() {
        navigator.showProfile(user, "chat_fragment")
    }
}