package com.topface.topface.ui.views.locker_view

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View

/**
 * Вьюмодель для LockerView
 * Created by petrp on 28.03.2017.
 */
class LockerViewViewModel(plcName: String) {

    val plc = ObservableField(plcName)
    val visibility = ObservableInt(View.GONE)

    fun release() {

    }
}