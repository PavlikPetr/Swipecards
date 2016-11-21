package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.content.res.TypedArray
import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import java.util.*


/**
 * ВьюМодел для заблёренного эксперимент4(3)
 */
class Experiment4WithBlurViewModel(binding: Experiment4Binding) : Experiment4BaseViewModel(binding) {

    companion object {
        private var mIsUserMale = if (App.get().profile.sex == Profile.BOY) true else false
        val girls = App.getContext().resources.obtainTypedArray(R.array.fake_girls_with_blur)
        val boys = App.getContext().resources.obtainTypedArray(R.array.fake_boys_with_blur)
    }

    override val fakeAvatars: ObservableField<List<Int>>
        get() = ObservableField<List<Int>>(Utils.randomImageRes(5, if (mIsUserMale) fromObtainToInt(girls) else fromObtainToInt(boys)))
    override val popupMessage: ObservableField<String>
        get() = ObservableField<String>(R.string.know_your_guests_by_sight.getString())
    override val imageLeftTop: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.eye4ex)
    override val imageRightBottom: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.eye_closed)
    override val imageUnderAvatar: ObservableField<Int>
        get() = ObservableField<Int>(R.drawable.glasses_for_experiment_popup)

    fun fromObtainToInt(ar: TypedArray): List<Int> {
        val usersFakeArray = ArrayList<Int>()
        for (i in 0..ar.length() - 1) {
            usersFakeArray.add(ar.getResourceId(i, R.drawable.girl_1))
        }
        return usersFakeArray
    }
}