package com.topface.topface.ui.fragments.dating.mutual_popup

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.annotation.IntDef
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString

class PopupMutualViewModel(val navigator: FeedNavigator, val mutualUser: FeedUser, val iDialogCloser: IDialogCloser, @MutualPopupType val popupType: Long) {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(DATING_TYPE, SYMPATHY_TYPE)
    annotation class MutualPopupType

    companion object{
        const val DATING_TYPE = 1L
        const val SYMPATHY_TYPE = 2L
    }

    val userPhoto = App.get().profile.photo
    val type = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND
    val userPlaceholderRes = ObservableInt((if (App.get().profile.sex == User.BOY) R.drawable.dialogues_av_man_big
    else R.drawable.dialogues_av_girl_small))

    val greenButtonText = ObservableField<String>()
    val borderlessButtonText = ObservableField<String>()
    init {
        when(popupType){
            DATING_TYPE-> {
                greenButtonText.set(R.string.start_dialog.getString())
                borderlessButtonText.set(R.string.continue_to_meet.getString())
            }
            SYMPATHY_TYPE-> {
//                greenButtonText.set(R.string.start_dialog.getString())
//                borderlessButtonText.set(R.string.continue_to_meet.getString())
            }
        }
    }


    val mutualUserPhoto = ObservableField(mutualUser.photo)
    val mutualPlaceholderRes = ObservableInt(if (mutualUser.sex == User.BOY) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_big)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()

    fun startDialog() {
        navigator.showChat(mutualUser, null, MutualPopupFragment.TAG)
        iDialogCloser.closeIt()
    }

    fun closePopup() = iDialogCloser.closeIt()
}