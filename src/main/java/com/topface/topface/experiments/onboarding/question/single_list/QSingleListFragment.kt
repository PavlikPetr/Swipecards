package com.topface.topface.experiments.onboarding.question.single_list

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.OnboardingQSingleListBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.appContext
import org.jetbrains.anko.layoutInflater

/**
 * Fragment for list with single selection question
 */
class QSingleListFragment: BaseFragment() {
    override fun needOptionsMenu() = false

    private val mViewModel by lazy {
        QSingleListFragmentViewModel()
    }

    private val mLayoutResId = R.layout.onboarding_q_single_list

    private val mTypeProvider by lazy {
        TypeProvider()
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mTypeProvider) {
            Bundle()
        }.addAdapterComponent(SingleListItemComponent())
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<OnboardingQSingleListBinding>(context.layoutInflater, mLayoutResId, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        mBinding.list.adapter = mAdapter
        mBinding.list.layoutManager = LinearLayoutManager(mBinding.appContext(), LinearLayoutManager.VERTICAL, false)

        return mBinding.root
    }
}