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
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

class QRangeFragment: BaseFragment() {
    companion object {
        const val EXTRA_DATA = "QRangeFragment.Extra.Data"
        fun newInstance(data: QuestionTypeFirst) = QRangeFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_DATA, data)
            }
        }
    }

    override fun needOptionsMenu() = false

    private val mViewModel by lazy {
        QRangeFragmentViewModel(arguments).apply {
            activity.registerLifeCycleDelegate(this)
        }

    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<QuestionnaireQRangeBinding>(context.layoutInflater, R.layout.questionnaire_q_range, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply { viewModel = mViewModel }.root

    override fun onDetach() {
        super.onDetach()
        activity.unregisterLifeCycleDelegate(mViewModel)
    }
}