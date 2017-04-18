package com.topface.topface.ui.fragments.dating.form

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import com.topface.topface.R
import com.topface.topface.data.Profile

/**
 *  Модельки для анкеты
 * Created by tiberal on 02.11.16.
 */

data class FormModel(var data: Pair<String, String>? = null, var userId: Int? = null, var formType: Int = -1,
                     val isEmptyItem: Boolean, @DrawableRes var iconRes: Int,
                     @ColorRes var formItemBackground: Int = R.color.transparent, var onRequestSended: (() -> Unit)? = null)

data class ParentModel(val data: String, val isTitleItem: Boolean, val icon: Int)

data class GiftsModel(val gifts: Profile.Gifts?, val userId: Int)

data class FakeGift(var diffTemp: Int = 0) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<FakeGift> = object : Parcelable.Creator<FakeGift> {
            override fun createFromParcel(source: Parcel): FakeGift = FakeGift(source)
            override fun newArray(size: Int): Array<FakeGift?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(diffTemp)
    }
}