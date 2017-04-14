package com.topface.topface.ui.fragments.dating.form

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.fragments.dating.ParentModel
import org.jetbrains.anko.dimen

class ParentItemViewModel(val data: ParentModel,
                          val itemPosition: Int) {
    val dividerVisibility = ObservableInt(if (data!=null) View.GONE else View.VISIBLE)
    val itemIcon = ObservableInt(data.icon)
    val titleText = ObservableField<String>(data.data)
    val titleTextSize = ObservableInt(getTextSize())
    val titleTextColor = ObservableInt(getTextColor())
    val bold = ObservableBoolean(data.isTitleItem)

    fun getTextSize() = if (data.isTitleItem) {
        App.getContext().dimen(R.dimen.dating_parent_item_title_size)
    } else {
        App.getContext().dimen(R.dimen.dating_parent_item_subtitle_size)
    }

    fun getTextColor() = if (data.isTitleItem) {
        App.getContext().resources.getColor(R.color.black_forms_color)
    } else {
        App.getContext().resources.getColor(R.color.gray_forms_color)
    }

    fun itemClick() {}
}