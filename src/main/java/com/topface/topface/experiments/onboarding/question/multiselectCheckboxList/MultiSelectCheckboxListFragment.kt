package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.MultiselectCheckboxListBinding
import com.topface.topface.experiments.onboarding.question.QuestionTypeFourth
import com.topface.topface.ui.add_to_photo_blog.TypeProvider
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.appContext
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

class MultiSelectCheckboxListFragment : BaseFragment() {

    companion object {
        const val EXTRA_DATA = "MultiSelectCheckboxListFragment.Extra.Data"

        fun newInstance(data: QuestionTypeFourth) = MultiSelectCheckboxListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MultiSelectCheckboxListFragment.EXTRA_DATA, data)
            }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<MultiselectCheckboxListBinding>(context.layoutInflater, R.layout.multiselect_checkbox_list, null, false)
    }

    private val mViewModel by lazy {
        MultiSelectCheckboxViewModel(arguments).apply {
            activity.registerLifeCycleDelegate(this)
        }
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(TypeProvider()) {
            Bundle()
        }.addAdapterComponent(MultiselectCheckboxlistItemComponent())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            mBinding.apply {
                viewModel = mViewModel
                recyclerView.adapter = mAdapter
                recyclerView.layoutManager = LinearLayoutManager(mBinding.appContext(), LinearLayoutManager.VERTICAL, false)
            }.root

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.release()
    }

    override fun onDetach() {
        super.onDetach()
        activity.unregisterLifeCycleDelegate(mViewModel)
    }
}