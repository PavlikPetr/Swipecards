package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.view.View
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.MultiselectListItem

class MultiSelectCheckboxItemViewModel(current: MultiselectListItem) : View.OnClickListener {

    private val mEventBus = App.getAppComponent().eventBus()

    var title = ObservableField<String>(current.title)
    var isSelected = ObservableBoolean(current.isSelected)
    var image = ObservableField<String>(current.image)
    var value = ObservableField<String>(current.value)

    override fun onClick(v: View?) {
        v?.let {
            isSelected.set(!isSelected.get())
            mEventBus.setData(CheckboxSelected(value.get()))
        }
    }


}