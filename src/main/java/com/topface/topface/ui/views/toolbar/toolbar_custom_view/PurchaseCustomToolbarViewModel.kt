package com.topface.topface.ui.views.toolbar.toolbar_custom_view

import android.databinding.ObservableField
import com.topface.topface.databinding.PurchaseToolbarAdditionalViewBinding
import com.topface.topface.viewModels.BaseViewModel

/**
 * Created by ppavlik on 07.11.16.
 * Вьюмоделька для кастомной вью в тулбаре покупок
 */

class PurchaseCustomToolbarViewModel(binding: PurchaseToolbarAdditionalViewBinding) : BaseViewModel<PurchaseToolbarAdditionalViewBinding>(binding) {
    val coins = ObservableField<String>()
    val likes = ObservableField<String>()
}