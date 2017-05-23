package com.topface.topface.ui.fragments.feed.enhanced.chat.stubs

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.utils.extensions.getDimen

abstract class BaseBuyVipViewModel(feedUser: FeedUser?): BaseViewModel() {

    abstract val title: ObservableField<String>
    abstract val stubText: ObservableField<String>
    abstract val buttonText: ObservableField<String>

    val mutualUserPhoto = ObservableField(feedUser?.photo)
    val mutualPlaceholderRes = ObservableInt(if (feedUser?.sex == User.BOY) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_big)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()
    val type = GlideTransformationType.CROP_CIRCLE_TYPE

    abstract fun buttonAction(): Unit?
}