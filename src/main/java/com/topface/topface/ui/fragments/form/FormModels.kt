package com.topface.topface.ui.fragments.form

import android.os.Parcel
import android.os.Parcelable
import com.topface.topface.data.Profile

/**
 *  Модельки для анкеты
 * Created by tiberal on 02.11.16.
 */

data class FormModel(var data: Pair<String, String>? = null) : IType, Parcelable {
    override fun getType() = 0

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<FormModel> = object : Parcelable.Creator<FormModel> {
            override fun createFromParcel(source: Parcel): FormModel = FormModel(source)
            override fun newArray(size: Int): Array<FormModel?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readSerializable() as Pair<String, String>?)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeSerializable(data)
    }
}

data class ParentModel(val data: String, val isTitleItem: Boolean, val icon: Int) : IType, Parcelable {
    override fun getType() = 1

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ParentModel> = object : Parcelable.Creator<ParentModel> {
            override fun createFromParcel(source: Parcel): ParentModel = ParentModel(source)
            override fun newArray(size: Int): Array<ParentModel?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), 1 == source.readInt(), source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(data)
        dest?.writeInt((if (isTitleItem) 1 else 0))
        dest?.writeInt(icon)
    }
}

data class GiftsModel(val gifts: Profile.Gifts?, val userId: Int) : IType {
    override fun getType() = 2
}