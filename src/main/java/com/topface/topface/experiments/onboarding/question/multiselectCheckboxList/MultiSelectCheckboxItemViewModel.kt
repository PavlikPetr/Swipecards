package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import android.widget.CheckBox
import com.topface.topface.R

/**
 * Created by mbulgakov on 29.03.17.
 */
class SelectLanguageItemViewModel(current: com.topface.topface.experiments.onboarding.question.multiselectCheckboxList.Language) {

    var title = android.databinding.ObservableField<String>(current.title)
    var isSelected = android.databinding.ObservableBoolean(current.isSelected)
    var imageLink = android.databinding.ObservableField(current.imageLink)
    var tag = android.databinding.ObservableField<String>(current.value)

}