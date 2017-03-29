package com.topface.topface.ui.views.toolbar.view_models

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.topface.topface.R
import com.topface.topface.databinding.QuestionnaireAdditionalToolbarViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.experiments.onboarding.question.CountersCustomToolbarViewModel
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.utils.Utils

/**
 * Вьюмодель тулбара со счетчиком для опросника
 * Created by ppavlik on 29.03.17.
 */
class QuestionnaireToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding,
                                                              mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {
    private val additionalViewModel by lazy {
        CountersCustomToolbarViewModel()
    }

    init {
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)
        val additionalViewBinding = DataBindingUtil.inflate<QuestionnaireAdditionalToolbarViewBinding>(LayoutInflater.from(context),
                R.layout.questionnaire_additional_toolbar_view, null, false)
        additionalViewBinding.viewModel = additionalViewModel
        binding.toolbarCustomView.addView(additionalViewBinding.root)
    }

    fun setCounter(currentPosition: Int? = null, questionsCount: Int? = null) {
        if (currentPosition != null) {
            additionalViewModel.currentPosition = currentPosition
        }
        if (questionsCount != null) {
            additionalViewModel.questionsCount = questionsCount
        }
    }

    override fun release() {
        super.release()
        additionalViewModel.release()
    }
}