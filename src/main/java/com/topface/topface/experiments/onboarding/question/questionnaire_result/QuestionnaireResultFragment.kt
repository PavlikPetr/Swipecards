package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.FoundedPeopleRequestBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.registerLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

class QuestionnaireResultFragment(val feedNavigator: IFeedNavigator): BaseFragment() {

    companion object {
        const val EXTRA_DATA = "QuestionnaireResultFragment.Extra.Data"
        fun newInstance(data: QuestionnaireResult) = QuestionnaireResultFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_DATA, data)
            }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<FoundedPeopleRequestBinding>(context.layoutInflater,
                R.layout.founded_people_request, null, false)
    }

    private val mViewModel by lazy {
        QuestionnaireResultViewModel(feedNavigator).apply {
            activity.registerLifeCycleDelegate(this)
        }
    }

    override fun needOptionsMenu() = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            mBinding.apply {
                viewModel = mViewModel
            }.root

}