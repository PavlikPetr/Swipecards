package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.FoundedPeopleRequestBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.registerLifeCycleDelegate
import org.jetbrains.anko.layoutInflater
import org.json.JSONObject

class QuestionnaireResultFragment : BaseFragment() {

    companion object {
        const val TAG = "QuestionnaireResultFragment"
        const val EXTRA_METHOD_NAME = "QuestionnaireResultFragment.Extra.MethodName"
        const val EXTRA_REQUEST_DATA = "QuestionnaireResultFragment.Extra.RequestData"
        fun newInstance(methodName: String, requestData: JSONObject
        ) = QuestionnaireResultFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_METHOD_NAME, methodName)
                putString(EXTRA_REQUEST_DATA, requestData.toString())
            }
        }
    }

    private val mApi by lazy {
        FeedApi(context, this)
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<FoundedPeopleRequestBinding>(context.layoutInflater,
                R.layout.founded_people_request, null, false)
    }

    private val mViewModel by lazy {
        QuestionnaireResultViewModel(arguments, mApi, FeedNavigator(activity as IActivityDelegate)).apply {
            activity.registerLifeCycleDelegate(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply { viewModel = mViewModel }.root

    override fun onDetach() {
        super.onDetach()
        mViewModel.release()
    }
}