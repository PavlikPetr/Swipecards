package com.topface.topface.experiments.onboarding.question

import android.os.Bundle
import android.support.v4.app.Fragment
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.AcQuestionnaireBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.fragments.TrackedLifeCycleActivity
import com.topface.topface.ui.views.toolbar.view_models.QuestionnaireToolbarViewModel
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONObject
import rx.Subscription

/**
 * Активити опросника
 * Created by petrp on 29.03.2017.
 */
class QuestionnaireActivity : TrackedLifeCycleActivity<AcQuestionnaireBinding>(), IQuestionNavigator {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mToolbarViewModel: QuestionnaireToolbarViewModel? = null
    private var mQuestionaireSubscription: Subscription? = null
    private val mRequestData = JSONObject()

    override fun getToolbarBinding(binding: AcQuestionnaireBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.ac_questionnaire

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) = QuestionnaireToolbarViewModel(toolbar)
            .apply { mToolbarViewModel = this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mQuestionaireSubscription = mEventBus
                .getObservable(UserChooseAnswer::class.java)
                .subscribe(shortSubscription {
                    showNextQuestion()
                })
    }

    private fun showNextQuestion() {

    }

    override fun onDestroy() {
        super.onDestroy()
        mQuestionaireSubscription.safeUnsubscribe()
    }

    override fun addQuestionScreen(fragment: Fragment?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showResultScreen() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}