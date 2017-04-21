package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.MultiselectListItem

class MultiSelectCheckboxItemViewModel(current: MultiselectListItem) {

    private val mEventBus = App.getAppComponent().eventBus()

    var title = ObservableField<String>(current.title)
    var isSelected = ObservableBoolean(current.isSelected)
    var image = ObservableField<String>(current.image)
    var value = ObservableField<String>(current.value)

    fun onClick() {
        isSelected.set(!isSelected.get())
        mEventBus.setData(CheckboxSelected(value.get()))
    }
}