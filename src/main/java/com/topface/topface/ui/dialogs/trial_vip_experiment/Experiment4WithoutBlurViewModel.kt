package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString

/**
 * ВьюМодел для незаблёренного, чистого как слеза - эксперимента4(3)
 */
class Experiment4WithoutBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {

    companion object {
        const val COUNT = 5
        private var mIsUserMale = if (App.get().profile.sex == Profile.BOY) true else false
        val girls = listOf(R.drawable.girl_1,
                R.drawable.girl_2,
                R.drawable.girl_3,
                R.drawable.girl_4,
                R.drawable.girl_5,
                R.drawable.girl_6,
                R.drawable.girl_7,
                R.drawable.girl_8,
                R.drawable.girl_9,
                R.drawable.girl_10)

        val boys = listOf(R.drawable.man_1,
                R.drawable.man_2,
                R.drawable.man_3,
                R.drawable.man_4,
                R.drawable.man_5,
                R.drawable.man_6,
                R.drawable.man_7,
                R.drawable.man_8,
                R.drawable.man_9,
                R.drawable.man_10)
    }

    override val fakeAvatars: ObservableField<List<Int>>
        get() = ObservableField<List<Int>>(Utils.randomImageRes(COUNT, if (mIsUserMale) girls else boys))
    override val popupMessage: ObservableField<String>
        get() = ObservableField<String>(if (mIsUserMale) R.string.write_beautiful_girls_without_limits.getString() else R.string.write_beautiful_boys_without_limits.getString())
    override val imageUnderAvatar: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.message_icon_red)
    override val imageLeftTop: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.time)
    override val imageRightBottom: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.pen)



}