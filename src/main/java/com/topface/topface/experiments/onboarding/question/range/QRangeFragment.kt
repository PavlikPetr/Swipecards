package com.topface.topface.experiments.onboarding.question.range

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.QuestionnaireQRangeBinding
import com.topface.topface.experiments.onboarding.question.QuestionTypeFirst
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater

class QRangeFragment: BaseFragment() {
    companion object {
        const val EXTRA_DATA = "QRangeFragment.Extra.Data"
        fun newInstance(data: QuestionTypeFirst) = QRangeFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_DATA, data)
            }
        }
        private const val STATE_MIN = "QRangeFragment.State.Min"
        private const val STATE_MAX = "QRangeFragment.State.Min"
    }

    override fun needOptionsMenu() = false

    private val mViewModel by lazy {
        QRangeFragmentViewModel(arguments)
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<QuestionnaireQRangeBinding>(context.layoutInflater, R.layout.questionnaire_q_range, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply { viewModel = mViewModel }.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            if (it.containsKey(STATE_MAX)) mViewModel.end.set(it.getInt(STATE_MAX))
            if (it.containsKey(STATE_MIN)) mViewModel.start.set(it.getInt(STATE_MIN))
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putInt(STATE_MAX, mViewModel.end.get())
            putInt(STATE_MIN, mViewModel.start.get())
        }
    }
}