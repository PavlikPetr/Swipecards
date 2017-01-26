package com.topface.topface.data.experiments

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

/**
 * Experiment config for new dating
 * Created by m.bayutin on 24.01.17.
 */
class NewDatingDesign : BaseExperiment {

    constructor() : super()
    constructor(_in: Parcel) : super(_in)

    val isKitKatWithNoTranslucent by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
        && !isEnabled
    }

    companion object {
        @JvmField val CREATOR = object : Parcelable.Creator<NewDatingDesign> {
            override fun createFromParcel(source: Parcel): NewDatingDesign = NewDatingDesign(source)
            override fun newArray(size: Int): Array<NewDatingDesign?> = arrayOfNulls(size)
        }
    }

    override fun isEnabled() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) super.isEnabled() else false

    // todo fill with correct value
    override fun getOptionsKey() = "newDatingDesign"
}