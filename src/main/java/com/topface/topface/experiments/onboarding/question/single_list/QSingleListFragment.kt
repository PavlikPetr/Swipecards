package com.topface.topface.experiments.onboarding.question.single_list

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.QuestionnaireQSingleListBinding
import com.topface.topface.experiments.onboarding.question.QuestionTypeSecond
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.appContext
import org.jetbrains.anko.layoutInflater

/**
 * Fragment for list with single selection question
 */
class QSingleListFragment: BaseFragment() {
    companion object {
        const val EXTRA_DATA = "QSingleListFragment.Extra.Data"
        fun newInstance(data: QuestionTypeSecond) = QSingleListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_DATA, data)
            }
        }
    }

    override fun needOptionsMenu() = false

    private val mViewModel by lazy {
        QSingleListFragmentViewModel(arguments)
    }

    private val mAdapter: CompositeAdapter by lazy {
        with(mViewModel) {
            CompositeAdapter(TypeProvider()) {
                Bundle()
            }.addAdapterComponent(SingleListItemComponent(this.fieldName, this.isEnabled))
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<QuestionnaireQSingleListBinding>(context.layoutInflater, R.layout.questionnaire_q_single_list, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply {
                viewModel = mViewModel
                list.adapter = mAdapter
                list.layoutManager = LinearLayoutManager(mBinding.appContext(), LinearLayoutManager.VERTICAL, false)
            }.root
}