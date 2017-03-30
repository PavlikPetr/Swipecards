package com.topface.topface.experiments.onboarding.question.text_input

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.QuestionnaireDigitInputLayoutBinding
import com.topface.topface.databinding.QuestionnaireTextInputLayoutBinding
import com.topface.topface.experiments.onboarding.question.InputValueSettings
import com.topface.topface.experiments.onboarding.question.digit_input.DigitInputFragment
import com.topface.topface.experiments.onboarding.question.digit_input.DigitInputFragmentViewModel
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater

/**
 * Фрагмент для третьего типа вопроса
 * Created by petrp on 29.03.2017.
 */
class TextInputFragment : BaseFragment() {
    companion object {
        const val EXTRA_DATA = "EnterValueFragment.Extra.Data"
        fun newInstance(data: InputValueSettings) = DigitInputFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_DATA, data)
            }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<QuestionnaireTextInputLayoutBinding>(context.layoutInflater,
                R.layout.questionnaire_text_input_layout, null, false)
    }

    private val mViewModel by lazy {
        TextInputFragmentViewModel(arguments)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply { viewModel = mViewModel }.root

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.release()
    }
}