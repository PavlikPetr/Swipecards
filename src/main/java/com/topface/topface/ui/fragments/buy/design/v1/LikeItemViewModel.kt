package com.topface.topface.ui.fragments.buy.design.v1

import android.databinding.ObservableField
import com.topface.framework.utils.Debug

class LikeItemViewModel {
    //TODO НИЖЕ ГОВНО ПОПРАВЬ ПАРЯ
    // тестовая инициализация полей
    val titleText = ObservableField<String>("13 likof")
    val priceText = ObservableField<String>("za 999 rub")
    fun buy() {
        Debug.log("--- buy likes")
    }
}