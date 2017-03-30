package com.topface.topface.ui.auth.fb_invitation

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import android.widget.CheckBox
import com.topface.topface.R

/**
 * Created by mbulgakov on 29.03.17.
 */
class SelectLanguageItemViewModel(current: Language) {

    var title = ObservableField<String>(current.title)
    var isSelected = ObservableBoolean(current.isSelected)
    var imageLink = ObservableField(current.imageLink)
    var tag = ObservableField<String>(current.value)

}