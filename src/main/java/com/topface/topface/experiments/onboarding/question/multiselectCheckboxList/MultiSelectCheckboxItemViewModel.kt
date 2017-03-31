package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.view.View
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.MultiselectListItem


/**
 * Created by mbulgakov on 29.03.17.
 */
class MultiSelectCheckboxItemViewModel(current: MultiselectListItem) {

    private val mEventBus = App.getAppComponent().eventBus()

    var title = ObservableField<String>(current.title)
    var isSelected = ObservableBoolean(current.isSelected)
    var image = ObservableField<String>(current.image)
    var value = ObservableField<String>(current.value)

    fun onItemClickListener()=
        object : View.OnClickListener {
            override fun onClick(v: View?) {
                v?.let {
                    isSelected.set(if (isSelected.get()) false else true)
                    mEventBus.setData(CheckboxSelected(value.get()))
                }
            }
        }

}