package com.topface.topface.experiments.onboarding.question.valueSetter

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.QuestionnaireEnterValueLayoutBinding
import com.topface.topface.experiments.onboarding.question.QuestionTypeThird
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater

/**
 * Фрагмент для третьего типа вопроса
 * Created by petrp on 29.03.2017.
 */
class EnterValueFragment : BaseFragment() {
    companion object {
        const val EXTRA_DATA = "EnterValueFragment.Extra.Data"
        fun newInstance(data: QuestionTypeThird) = EnterValueFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_DATA, data)
            }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<QuestionnaireEnterValueLayoutBinding>(context.layoutInflater,
                R.layout.questionnaire_enter_value_layout, null, false)
    }

    private val mViewModel by lazy {
        EnterValueFragmentViewModel(arguments)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply { viewModel = mViewModel }.root
}