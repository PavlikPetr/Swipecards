package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.fromObtainToInt
import com.topface.topface.utils.extensions.getString

/**
 * ВьюМодел для незаблёренного, чистого,как слеза, эксперимента4(3)
 */
class Experiment4WithoutBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {

    companion object {
        private var mIsUserMale = if (App.get().profile.sex == Profile.BOY) true else false
    }

    override val fakeAvatars: ObservableField<List<Int>>
        get() = ObservableField<List<Int>>(Utils.randomImageRes(5, if (mIsUserMale) R.array.fake_girls_without_blur.fromObtainToInt(R.drawable.girl_1)
        else R.array.fake_boys_without_blur.fromObtainToInt(R.drawable.man_1)))
    override val popupMessage: ObservableField<String>
        get() = ObservableField<String>(if (mIsUserMale) R.string.write_beautiful_girls_without_limits.getString() else R.string.write_beautiful_boys_without_limits.getString())
    override val imageUnderAvatar: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.message_icon_red)
    override val imageLeftTop: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.time)
    override val imageRightBottom: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.pen)

}