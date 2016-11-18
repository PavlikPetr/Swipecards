package com.topface.topface.ui.dialogs.trial_vip_experiment

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString

/**
 * ВьюМодел для заблёренного эксперимент4(3)
 */
class Experiment4WithBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {

    private var mIsUserMale = if (App.get().profile.sex == Profile.BOY) true else false

    val girls = listOf(R.drawable.girl_blur_1,
            R.drawable.girl_blur_2,
            R.drawable.girl_blur_3,
            R.drawable.girl_blur_4,
            R.drawable.girl_blur_5,
            R.drawable.girl_blur_6,
            R.drawable.girl_blur_7,
            R.drawable.girl_blur_8,
            R.drawable.girl_blur_9,
            R.drawable.girl_blur_10)

    val boys = listOf(R.drawable.man_blur_1,
            R.drawable.man_blur_2,
            R.drawable.man_blur_3,
            R.drawable.man_blur_4,
            R.drawable.man_blur_5,
            R.drawable.man_blur_6,
            R.drawable.man_blur_7,
            R.drawable.man_blur_8,
            R.drawable.man_blur_9,
            R.drawable.man_blur_10)

    init {
        popupMessage.set(R.string.know_your_guests_by_sight.getString())
        title.set(R.string.know_your_guests.getString())
        imageLeftTop.set(R.drawable.eye4ex)
        fakeAvatars.set(Utils.randomImageRes(5, if (mIsUserMale) girls else boys))
        imageRightBottom.set(R.drawable.eye_closed)
        imageUnderAvatar.set(R.drawable.glasses_for_experiment_popup)
    }
}