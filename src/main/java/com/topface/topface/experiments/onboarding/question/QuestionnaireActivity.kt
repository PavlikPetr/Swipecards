package com.topface.topface.experiments.onboarding.question

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.AcQuestionnaireBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.fragments.TrackedLifeCycleActivity
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONObject
import rx.Subscription

/**
 * Активити опросника
 * Created by petrp on 29.03.2017.
 */
class QuestionnaireActivity : TrackedLifeCycleActivity<AcQuestionnaireBinding>(), IQuestionNavigator {

    companion object {
        fun getIntent() =
                Intent(App.getContext(), QuestionnaireActivity::class.java).apply {
                }
    }

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private val mQuestionNavigator by lazy {
        QuestionScreenNavigator(arrayOf(QuestionSettings(type = 3,
                typeThird = QuestionTypeThird(title = "Укажи твой рост ",
                        min = ValueConditions(value = 130, errorMessage = "Минимальное значение для роста 130 см"),
                        max = ValueConditions(value = 250, errorMessage = "Максимальное значение для роста 250 см"),
                        unit = "см",
                        fieldName = "height",
                        hint = "Ваш рост"
                ))), questionNavigator = this)
    }

    private var mToolbarViewModel: QuestionnaireToolbarViewModel? = null
    private var mQuestionaireSubscription: Subscription? = null
    private val mRequestData = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mQuestionaireSubscription = mEventBus
                .getObservable(UserChooseAnswer::class.java)
                .subscribe(shortSubscription {
                    mQuestionNavigator.show()
                })
        // запускаем показы
        mQuestionNavigator.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mQuestionaireSubscription.safeUnsubscribe()
    }

    override fun addQuestionScreen(fragment: Fragment?) =
            fragment?.let {
                mToolbarViewModel?.additionalViewModel?.setCounterTitle(mQuestionNavigator.getCurrentPosition() + 1, mQuestionNavigator.getTotalPOsition())
                supportFragmentManager.beginTransaction().replace(R.id.content, fragment, null).commit()
                Unit
            } ?: Unit

    override fun showResultScreen() {
        mToolbarViewModel?.additionalViewModel?.visibility?.set(View.GONE)
        //todo временно закрываю активити для тестирования. Как будет готов экран F2, надо здесь вызвать его показ
        finish()
    }

    override fun getToolbarBinding(binding: AcQuestionnaireBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.ac_questionnaire

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) = QuestionnaireToolbarViewModel(toolbar, this)
            .apply { mToolbarViewModel = this }

    override fun onUpButtonClick() {
        //ничего не делаем
    }
}