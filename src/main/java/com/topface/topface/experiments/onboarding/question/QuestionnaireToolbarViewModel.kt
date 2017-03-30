package com.topface.topface.experiments.onboarding.question

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.QuestionnaireAdditionalToolbarViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel
import com.topface.topface.utils.Utils

/**
 * Вьюмодель тулбара со счетчиком для опросника
 * Created by ppavlik on 29.03.17.
 */
class QuestionnaireToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding,
                                                              mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {
    val additionalViewModel by lazy {
        CountersCustomToolbarViewModel()
    }

    init {
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)
        shadowVisibility.set(View.GONE)
        upIcon.set(R.drawable.empty_res)
        val additionalViewBinding = DataBindingUtil.inflate<QuestionnaireAdditionalToolbarViewBinding>(LayoutInflater.from(context),
                R.layout.questionnaire_additional_toolbar_view, null, false)
        additionalViewBinding.viewModel = additionalViewModel
        binding.toolbarCustomView.addView(additionalViewBinding.root)
    }

    override fun release() {
        super.release()
        additionalViewModel.release()
    }
}