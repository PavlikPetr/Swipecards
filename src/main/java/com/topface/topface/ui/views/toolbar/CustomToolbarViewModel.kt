package com.topface.topface.ui.views.toolbar

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.databinding.CustomTitleAndSubtitleToolbarAdditionalViewBinding
import com.topface.topface.viewModels.BaseViewModel

/**
 * Created by ppavlik on 07.11.16.
 */

class CustomToolbarViewModel(binding: CustomTitleAndSubtitleToolbarAdditionalViewBinding) : BaseViewModel<CustomTitleAndSubtitleToolbarAdditionalViewBinding>(binding) {
    val title = ObservableField<String>()
    val subTitle = ObservableField<String>()
    val titleVisibility = ObservableInt(View.VISIBLE)
    val subTitleVisibility = ObservableInt(View.VISIBLE)
    val isOnline = ObservableBoolean()
}