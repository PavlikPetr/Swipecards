package com.topface.topface.api

import android.os.Parcel
import android.os.Parcelable
import com.topface.topface.ui.new_adapter.ExpandableItem

class UnreadStatePair(var from: Boolean = false, var to: Boolean = false) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<UnreadStatePair> = object : Parcelable.Creator<UnreadStatePair> {
            override fun createFromParcel(`in`: Parcel) = UnreadStatePair(`in`.readByte().toInt() == 1, `in`.readByte().toInt() == 1).apply {
                wasFromInited = `in`.readByte().toInt() == 1
            }

            override fun newArray(size: Int): Array<UnreadStatePair?> = arrayOfNulls(size)
        }


    }

    var wasFromInited: Boolean = false

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (wasFromInited) 1 else 0).toByte())
        dest.writeByte((if (from) 1 else 0).toByte())
        dest.writeByte((if (to) 1 else 0).toByte())
    }
}