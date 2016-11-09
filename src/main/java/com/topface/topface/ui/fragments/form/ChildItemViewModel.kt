package com.topface.topface.ui.fragments.form

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View

/**
 * Моделька ребеночка в анкете
 * Created by tiberal on 07.11.16.
 */
class ChildItemViewModel(data: FormModel) {

    val title = ObservableField<String>(data.data?.first)
    val subTitle = ObservableField<String>(data.data?.second)
    val isRequestButtonVisible = ObservableInt(if (data.data?.second.isNullOrEmpty()) View.VISIBLE else View.INVISIBLE)

    fun sendInfoRequest() {

    }

}