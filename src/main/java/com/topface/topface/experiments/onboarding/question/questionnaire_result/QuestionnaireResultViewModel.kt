package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Photo
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getDimen

class QuestionnaireResultViewModel(val navigator: IFeedNavigator,  val userSearchList: List<FeedUser>) {

    val firstAvatar = ObservableField<Photo>()
    val secondAvatar = ObservableField<Photo>()
    val thirdAvatar = ObservableField<Photo>()
    val fourthAvatar = ObservableField<Photo>()
    val fifthAvatar = ObservableField<Photo>()

    val type = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND
    val avatarPlaceholderRes = ObservableField((if (userSearchList.get(0).sex == User.BOY) R.drawable.dialogues_av_man_big
    else R.drawable.dialogues_av_girl_small))

    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()

    val foundTitle = ObservableField<String>()
    val buyMessage = ObservableField<String>()

    fun onBuyButtonClick() {
    }
}