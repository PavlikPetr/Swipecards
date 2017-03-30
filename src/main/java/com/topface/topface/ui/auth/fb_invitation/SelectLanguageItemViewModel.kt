package com.topface.topface.ui.auth.fb_invitation

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import android.widget.CheckBox

/**
 * Created by mbulgakov on 29.03.17.
 */
class SelectLanguageItemViewModel(current: Language) {

    var title = ObservableField<String>(current.title)
    var isSelected = ObservableBoolean(current.isSelected)
    var image = ObservableInt()
    var tag = ObservableField<String>(current.value)

    init {

    }


    var onItemClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            (v as CheckBox)?.let {
                val currentSKA = v.getTag()
                isSelected.set(true)

                //todo НАДО ОПОВЕСТИТЬ СПИСОК О ТОМ, ЧТО ЧУВАК ВЫБРАН
            }
        }
    }
}