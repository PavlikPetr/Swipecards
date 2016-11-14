package com.topface.topface.ui.fragments.dating.form

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.new_adapter.ExpandableItem
import org.jetbrains.anko.dimen

class ParentItemViewModel(val data: ExpandableItem<ParentModel>,
                          val itemPosition: Int) {
    val dividerVisibility = ObservableInt(if (data.isExpandable()) View.GONE else View.VISIBLE)
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

    fun itemClick() {}
}