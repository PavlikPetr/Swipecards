package com.topface.topface.ui.fragments.dating.dating_redesign

import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Photo
import com.topface.topface.data.Profile
import com.topface.topface.data.User
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.glide.tranformation.GlideTransformationType
import rx.Subscription
import javax.inject.Inject

/**
 * Created by mbulgakov on 17.02.17.
 */
class PopupMutualViewModel(val navigator: IFeedNavigator, val mutualUser: FeedUser) {

    val userPhoto = ObservableField<Photo>()
    val type = GlideTransformationType.CROP_CIRCLE_TYPE
    val userPlaceholderRes = ObservableField<Int>()
    val onLineCircle = ObservableField(R.dimen.dialog_online_circle.getDimen())

    val mutualUserPhoto = ObservableField(mutualUser.photo)
    val mutualPlaceholderRes = ObservableField(if (mutualUser.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small)
    val mutualType = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()

    var profileSubscription: Subscription
    @Inject lateinit var state: TopfaceAppState

    init {
        val user = App.get().profile
        setUserAvatar(user)
        prepareUserPlaceholder(user)
        App.get().inject(this)
        profileSubscription = state.getObservable(Profile::class.java).subscribe {
            setUserAvatar(it)
            prepareUserPlaceholder(it)
        }
    }

    private fun setUserAvatar(profile: Profile) = userPhoto.set(profile.photo)

    private fun prepareUserPlaceholder(profile: Profile) = userPlaceholderRes.set(if (profile.sex == User.BOY) R.drawable.dialogues_av_man_small
    else R.drawable.dialogues_av_girl_small)

    fun startDialog() = navigator.showChat(mutualUser,null)

    fun closePopup() {

    }

}