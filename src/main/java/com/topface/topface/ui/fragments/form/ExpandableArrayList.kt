package com.topface.topface.ui.fragments.form

import java.util.*

/**
 * Выпадающий массив. Каждый итем содержит данные и массви дополнителных данных, которые
 * при необходимости будут встроены/удалены из рутовую коллекции.
 * Created by tiberal on 30.10.16.
 */
class ExpandableArrayList<T>(private val mCallback: ExpandableListCallback? = null) : ExpandableList<T> {

    private val sourceData = ArrayList<ExpandableItem<T>>()

    override fun addExpandableItem(data: T, dataSequence: MutableList<T>, expandNow: Boolean) {
        if (dataSequence.isNotEmpty()) {
            sourceData.add(ExpandableItem(data, dataSequence))
        } else {
            sourceData.add(ExpandableItem(data))
        }
        if (expandNow) {
            expandItem(sourceData.count() - 1)
        } else {
            mCallback?.onInserted(0, 1)
        }
    }

    operator override fun get(position: Int) = sourceData[position]

    fun clear() = with(sourceData.size) {
        sourceData.clear()
        mCallback?.onRemoved(0, this)
    }

    @Synchronized override fun expandItem(position: Int) = with(sourceData[position]) {
        if (isExpandable()) {
            val data = dataSequence
            if (!isExpanded && data != null) {
                sourceData.addAll(position + 1, data)
                mCallback?.onInserted(position + 1, data.count())
                isExpanded = true
            }
        }
    }

    @Synchronized override fun constrictItem(position: Int) = with(sourceData[position]) {
        if (isExpandable()) {
            val data = dataSequence
            if (isExpanded && data != null) {
                sourceData.removeAll(data)
                mCallback?.onRemoved(position + 1, data.count())
                isExpanded = false
            }
        }
    }

    override fun getSize() = sourceData.size

    fun getData() = sourceData
}

