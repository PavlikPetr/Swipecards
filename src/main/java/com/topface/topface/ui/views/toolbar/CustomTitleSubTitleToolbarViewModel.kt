package com.topface.topface.ui.views.toolbar

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.topface.topface.R
import com.topface.topface.databinding.CustomTitleAndSubtitleToolbarAdditionalViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.views.toolbar.toolbar_custom_view.CustomToolbarViewModel
import com.topface.topface.utils.extensions.safeUnsubscribe
import rx.subscriptions.CompositeSubscription

/**
 * Created by petrp on 09.10.2016.
 * вью модель тулбара кактарая по дефолту имеет upIcon в виде стрелки влево и кастомный title/subtitle
 */

class CustomTitleSubTitleToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {
    var extraViewModel: CustomToolbarViewModel

    init {
        title.set("")
        subTitle.set("")
        val additionalViewBinding = DataBindingUtil.inflate<CustomTitleAndSubtitleToolbarAdditionalViewBinding>(LayoutInflater.from(context),
                R.layout.custom_title_and_subtitle_toolbar_additional_view, null, false)
        extraViewModel = CustomToolbarViewModel(additionalViewBinding)
        additionalViewBinding.viewModel = extraViewModel
        binding.toolbarCustomView.addView(additionalViewBinding.root)
    }

    override fun release() {
        super.release()
        extraViewModel.release()
    }
}
