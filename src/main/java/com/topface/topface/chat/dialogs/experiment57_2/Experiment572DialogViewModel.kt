package com.topface.topface.chat.dialogs.experiment57_2

import android.databinding.ObservableField
import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.chat.dialogs.experiment57_2.Experiment572DialogFragment.Companion.ARG_USER
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString

class Experiment572DialogViewModel(data: Bundle, val action:() -> Unit) {

    private val mUser: FeedUser = data.getParcelable(ARG_USER)

    val userMessage = ObservableField(String.format(
            (if (mUser.sex == User.BOY) R.string.chat_buy_vip_popular_male else R.string.chat_buy_vip_popular_female).getString()
            , mUser.firstName)
    )
    val vipMessage = ObservableField(
            (if (mUser.sex == User.BOY)R.string.buy_vip_to_chat_male else R.string.buy_vip_to_chat_female).getString()
    )
    val photo = ObservableField(mUser.photo)
    val placeholderResId = ObservableField(if (mUser.sex == User.BOY) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_big)
    val photoTransformType = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()
    fun onButtonClick() = action()
}