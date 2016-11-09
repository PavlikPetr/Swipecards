package com.topface.topface.ui.fragments.form

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.R
import org.jetbrains.anko.dimen

/**
 * Created by tiberal on 03.11.16.
 */
class ParentItemViewModel(val data: ExpandableItem<ParentModel>,
                          val expandableList: ExpandableList<*>?,
                          val itemPosition: Int) {
    val itemIcon = ObservableInt(data.data?.icon ?: 0)
    val titleText = ObservableField<String>(data.data?.data)
    val titleTextSize = ObservableInt(getTextSize())
    val titleTextColor = ObservableInt(getTextColor())
    val bold = ObservableBoolean(data.data?.isTitleItem ?: false)

    fun getTextSize() = if (data.data?.isTitleItem ?: false) {
        App.getContext().dimen(R.dimen.dating_parent_item_title_size)
    } else {
        App.getContext().dimen(R.dimen.dating_parent_item_subtitle_size)
    }

    fun getTextColor() = if (data.data?.isTitleItem ?: false) {
        App.getContext().resources.getColor(R.color.black_forms_color)
    } else {
        App.getContext().resources.getColor(R.color.gray_forms_color)
    }

    fun itemClick() {
        if (data.isExpandable()) {
            if (data.isExpanded) {
                expandableList?.constrictItem(itemPosition)
            } else {
                expandableList?.expandItem(itemPosition)
            }
        }
    }
}