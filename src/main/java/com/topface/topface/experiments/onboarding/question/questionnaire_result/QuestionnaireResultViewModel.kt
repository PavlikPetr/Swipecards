package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.databinding.ObservableLong
import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.data.User
import com.topface.topface.experiments.onboarding.question.QuestionnaireResult
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.utils.extensions.getDimen

class QuestionnaireResultViewModel(bundle: Bundle) {

    private val mData: QuestionnaireResult? = bundle.getParcelable(QuestionnaireResultFragment.EXTRA_DATA)

    val userList = mData?.users

    val foundTitle = ObservableField<String>(mData?.foundtitle)
    val buyMessage = ObservableField<String>(mData?.buyMessage)

    val firstAvatar: ObservableField<Photo> = ObservableField()
    val secondAvatar: ObservableField<Photo> = ObservableField()
    val thirdAvatar: ObservableField<Photo> = ObservableField()
    val fourthAvatar: ObservableField<Photo> = ObservableField()
    val fifthAvatar: ObservableField<Photo> = ObservableField()

    val avatarPlaceholderRes = ObservableInt()
    val type = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND

    val doNextSlide = ObservableBoolean(false)

    val startOffSettMedial = ObservableLong(500)
    val startOffSettLateral = ObservableLong(700)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()

    val productId = mData?.productId

    init {
        userList?.let {
            firstAvatar.set(it.get(0).photo)
            secondAvatar.set(it.get(1).photo)
            thirdAvatar.set(it.get(2).photo)
            fourthAvatar.set(it.get(3).photo)
            fifthAvatar.set(it.get(4).photo)

            avatarPlaceholderRes.set((if (it.get(0).sex == User.BOY) R.drawable.dialogues_av_man_big
            else R.drawable.dialogues_av_girl_small))
        }
    }

    fun onBuyButtonClick() {
    }
}