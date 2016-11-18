package com.topface.topface.ui.dialogs.trial_vip_experiment

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import java.util.*

/**
 * ВьюМодел для незаблёренного, чистого как слеза - эксперимента4(3)
 */
class Experiment4WithoutBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {

    private var mIsUserMale = if (App.get().profile.sex == Profile.BOY) true else false

    init {
        popupMessage.set(if (mIsUserMale) R.string.write_beautiful_girls_without_limits.getString() else R.string.write_beautiful_boys_without_limits.getString())
        fakeAvatars.set(getFakeAvatars())
        imageLeftTop.set(R.drawable.time)
        imageRightBottom.set(R.drawable.pen)
        imageUnderAvatar.set(R.drawable.message_icon_red)
    }

    fun getFakeAvatars(): ArrayList<Int>? {
        return Utils.getRandomArrayFromResourses(5,
                if (mIsUserMale) R.array.fake_girl_avatars_without_blur else R.array.fake_boy_avatars_without_blur,
                if (mIsUserMale) R.drawable.man_1 else R.drawable.girl_1)
    }

}