package com.topface.topface.ui.fragments.dating.design.v1

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getDimen

class PopupMutualViewModel(val navigator: FeedNavigator, val mutualUser: FeedUser, val iDialogCloser: IDialogCloser) {

    val userPhoto = App.get().profile.photo
    val type = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND
    val userPlaceholderRes = ObservableField((if (App.get().profile.sex == User.BOY) R.drawable.dialogues_av_man_big
    else R.drawable.dialogues_av_girl_small))

    val mutualUserPhoto = ObservableField(mutualUser.photo)
    val mutualPlaceholderRes = ObservableField(if (mutualUser.sex == User.BOY) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_big)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()

    fun startDialog() {
        navigator.showChat(mutualUser, null)
        iDialogCloser.closeIt()
    }

    fun closePopup() = iDialogCloser.closeIt()

}