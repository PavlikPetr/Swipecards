package com.topface.topface.api

import android.os.Parcel
import android.os.Parcelable
import com.topface.scruffy.utils.readBoolean
import com.topface.scruffy.utils.writeBoolean

data class UnreadStatePair(var from: Boolean = false, var to: Boolean = false, var wasFromInited: Boolean = false)
    : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<UnreadStatePair> = object : Parcelable.Creator<UnreadStatePair> {
            override fun createFromParcel(`in`: Parcel) =
                    UnreadStatePair(`in`.readBoolean(), `in`.readBoolean(), `in`.readBoolean())

            override fun newArray(size: Int): Array<UnreadStatePair?> = arrayOfNulls(size)
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeBoolean(wasFromInited)
        writeBoolean(from)
        writeBoolean(to)
    }
}