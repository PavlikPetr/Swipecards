package com.topface.topface.ui.auth.fb_invitation

import android.database.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import android.widget.CheckBox

/**
 * Created by mbulgakov on 29.03.17.
 */
class SelectLanguageItemViewModel(current: Language) {
    var tvName = ObservableField<String>(current.name)
    var tvEmailId= ObservableField<String>(current.emailId)
    var isChecked = ObservableBoolean(current.isSelected)
    var tag = ObservableInt()

    var onItemClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            (v as CheckBox)?.let {
                val currentSKA = v.getTag()
                isChecked.set(true)

                //todo НАДО ОПОВЕСТИТЬ СПИСОК О ТОМ, ЧТО ЧУВАК ВЫБРАН
            }
        }
    }
}