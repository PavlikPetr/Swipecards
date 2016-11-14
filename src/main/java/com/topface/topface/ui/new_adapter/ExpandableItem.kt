package com.topface.topface.ui.new_adapter

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.reflect.TypeToken
import com.topface.framework.JsonUtils

/**
 * Враппер итемя выпадающей коллекции
 * Created by tiberal on 31.10.16.
 */
class ExpandableItem<T> : Parcelable {

    constructor(source: Parcel) {
        val bundle = source.readBundle()
        data = JsonUtils.fromJson(bundle.getString("data"), object : TypeToken<T>() {})
        isChild = source.readByte().toInt() == 1
        isExpanded = source.readByte().toInt() == 1
    }

    constructor(data: T) {
        this.data = data
    }

    constructor(data: T, dataSequence: MutableList<T>) : this(data) {
        this.dataSequence = mutableListOf<ExpandableItem<T>>().apply {
            dataSequence.forEach {
                this@apply.add(ExpandableItem(it).apply { isChild = true })
            }
        }
    }

    var dataSequence: MutableList<ExpandableItem<T>>? = null
    var data: T? = null
    var isExpanded = false
    var isChild = false

    fun isExpandable() = dataSequence != null

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ExpandableItem<*>> = object : Parcelable.Creator<ExpandableItem<*>> {
            override fun createFromParcel(source: Parcel): ExpandableItem<*> = ExpandableItem<Parcelable>(source)
            override fun newArray(size: Int): Array<ExpandableItem<*>?> = arrayOfNulls(size)
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.let {
            val v = Bundle()
            v.putString("data", JsonUtils.toJson(data))
            it.writeBundle(v)
            it.writeByte((if (isChild) 1 else 0).toByte())
            it.writeByte((if (isExpanded) 1 else 0).toByte())
        }
    }
}