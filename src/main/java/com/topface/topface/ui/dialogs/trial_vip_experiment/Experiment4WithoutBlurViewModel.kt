package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.extensions.getString
import java.util.*

/**
 * Created by mbulgakov on 15.11.16.
 */
class Experiment4WithoutBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {


    private val AVATARS_ID_ARRAY_LENGTH = 5
    private val profile: Profile = App.get().profile
    private var mIsUserMale = if (profile.sex == Profile.BOY) true else false

    init {
        popupMessage = ObservableField(if (mIsUserMale) R.string.write_beautiful_girls_without_limits.getString() else R.string.write_beautiful_boys_without_limits.getString())
        title = ObservableField(if (mIsUserMale) R.string.write_any_girl.getString() else R.string.write_any_boy.getString())
        fakeAvatars.set(getFakeAvatars())
        imageLeftTop = ObservableField(R.drawable.time)
        imageRightBottom = ObservableField(R.drawable.pen)
        imageUnderAvatar = ObservableField(R.drawable.message_icon_red)
    }

    private fun getFakeAvatars(): ArrayList<Int> {
        val arrayId = if (mIsUserMale) R.array.fake_girl_avatars_without_blur else R.array.fake_boy_avatars_without_blur
        val avatarsIdArray = ArrayList<Int>()
        var randomValue: Int
        val imgs = App.getContext().resources.obtainTypedArray(arrayId)
        val usersFakeArray = ArrayList<Int>()
        for (i in 0..imgs.length() - 1) {
            usersFakeArray.add(imgs.getResourceId(i, if (mIsUserMale) R.drawable.man_1 else R.drawable.girl_1))
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