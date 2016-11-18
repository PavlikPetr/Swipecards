package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.R.drawable.man_1
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import java.util.*

/**
 * ВьюМодел для заблёренного эксперимент4(3)
 */
class Experiment4WithBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {

    private var mIsUserMale = if (App.get().profile.sex == Profile.BOY) true else false

    init {
        popupMessage.set(R.string.know_your_guests_by_sight.getString())
        title.set(R.string.know_your_guests.getString())
        imageLeftTop.set(R.drawable.eye4ex)
        fakeAvatars.set(getFakeAvatars())
        imageRightBottom.set(R.drawable.eye_closed)
        imageUnderAvatar.set(R.drawable.glasses_for_experiment_popup)
    }

    fun getFakeAvatars(): ArrayList<Int>? {
        return Utils.getRandomArrayFromResourses(5,
                if (mIsUserMale) R.array.fake_girl_avatars_with_blur else R.array.fake_boy_avatars_with_blur,
                if (mIsUserMale) R.drawable.man_blur_1 else R.drawable.girl_blur_4)
    }

}