package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.R.drawable.man_1
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.extensions.getString
import java.util.*

/**
 * ВьюМодел для заблеренного эксперимент4(3)
 */
class Experiment4WithBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {

    private val AVATARS_ID_ARRAY_LENGTH = 5
    private val profile: Profile = App.get().profile
    private var mIsUserMale = if (profile.sex == Profile.BOY) true else false

    init {
        popupMessage = ObservableField(R.string.know_your_guests_by_sight.getString())
        title = ObservableField(R.string.know_your_guests.getString())
        fakeAvatars.set(getFakeAvatars())
        imageLeftTop = ObservableField(R.drawable.eye4ex)
        imageRightBottom = ObservableField(R.drawable.eye_closed)
        imageUnderAvatar = ObservableField(R.drawable.glasses_for_experiment_popup)
    }

    private fun getFakeAvatars(): ArrayList<Int> {

        val arrayId = if (mIsUserMale) R.array.fake_girl_avatars_with_blur else R.array.fake_boy_avatars_with_blur
        val avatarsIdArray = ArrayList<Int>()
        var randomValue: Int
        val imgs = App.getContext().resources.obtainTypedArray(arrayId)
        val usersFakeArray = ArrayList<Int>()
        for (i in 0..imgs.length() - 1) {
            usersFakeArray.add(imgs.getResourceId(i, if (mIsUserMale) man_1 else R.drawable.girl_1))
        }

        for (i in 0..AVATARS_ID_ARRAY_LENGTH - 1) {
            var iterCounter = 0
            do {
                iterCounter++
                randomValue = Random().nextInt(usersFakeArray.size - 1)

            } while (avatarsIdArray.contains(usersFakeArray[randomValue]) || iterCounter < 30)
            avatarsIdArray.add(usersFakeArray[randomValue])
        }
        return avatarsIdArray
    }


}