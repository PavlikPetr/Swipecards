package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.utils.extensions.getDimen

class FriendMessageViewModel(item: HistoryItem, itemPosition: Int, feedUser: FeedUser?)
    : BaseMessageViewModel(item, itemPosition), IAvatarVisible {
    override val isAvatarVisible = item.isAvatarVisible
    val photo = ObservableField(feedUser?.photo)
    val placeholderResId = ObservableField(if (feedUser?.sex == User.BOY) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_big)
    val photoTransformType = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()
}