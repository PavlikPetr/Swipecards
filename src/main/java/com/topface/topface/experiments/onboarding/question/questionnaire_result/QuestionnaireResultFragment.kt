package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.os.Bundle
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.FoundedPeopleRequestBinding
import com.topface.topface.experiments.onboarding.question.QuestionnaireResult
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.utils.registerLifeCycleDelegate
import kotlin.properties.Delegates

class QuestionnaireResultFragment : AbstractDialogFragment() {

    companion object {
        const val TAG = "QuestionnaireResultFragment"
        const val EXTRA_DATA = "QuestionnaireResultFragment.Extra.Data"
        fun getInstance(data: QuestionnaireResult
        ) = QuestionnaireResultFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_DATA, data)
            }
        }
    }

    override fun getDialogLayoutRes() = R.layout.founded_people_request

    private var mBinding by Delegates.notNull<FoundedPeopleRequestBinding>()

    override fun isModalDialog() = false

    override fun initViews(root: View?) {
        mBinding = FoundedPeopleRequestBinding.bind(root)
        mBinding.setViewModel(mViewModel)
    }

    private val mViewModel by lazy {
        QuestionnaireResultViewModel(arguments).apply {
            activity.registerLifeCycleDelegate(this)
        }
    }
}