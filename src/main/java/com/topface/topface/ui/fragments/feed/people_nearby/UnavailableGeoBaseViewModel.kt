package com.topface.topface.ui.fragments.feed.people_nearby

import android.databinding.ObservableField
import com.topface.topface.databinding.LayoutUnavailableGeoBinding
import com.topface.topface.viewModels.BaseViewModel

/**
 * Базовая вьюмодель заглушки при проблемах с пермишином для гео
 * Created by petrp on 31.10.2016.
 */
abstract class UnavailableGeoBaseViewModel(binding: LayoutUnavailableGeoBinding,
                                           textExplanation: String,
                                           buttonText: String) : BaseViewModel<LayoutUnavailableGeoBinding>(binding) {
    val text = ObservableField<String>()
    val button = ObservableField<String>()
    abstract fun onButtonClick()

    init {
        text.set(textExplanation)
        button.set(buttonText)
    }
}