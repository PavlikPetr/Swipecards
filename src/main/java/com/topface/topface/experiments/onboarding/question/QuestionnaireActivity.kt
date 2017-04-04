package com.topface.topface.experiments.onboarding.question

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.AcQuestionnaireBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.experiments.onboarding.question.questionnaire_result.QuestionnaireResultFragment
import com.topface.topface.ui.BaseFragmentActivity
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.fragments.buy.GpPurchaseActivity
import com.topface.topface.ui.views.toolbar.view_models.InvisibleToolbarViewModel
import com.topface.topface.utils.extensions.showShortToast
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONException
import org.json.JSONObject
import rx.Subscription
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Активити опросника
 * Created by petrp on 29.03.2017.
 */
class QuestionnaireActivity : BaseFragmentActivity<AcQuestionnaireBinding>(), IQuestionNavigator {

    companion object {
        private const val RESPONSE_DATA = "QuestionnaireActivity.Response.Data"
        private const val CURRENT_QUESTION_POSITION = "QuestionnaireActivity.Current.Question.Position"
        private const val REQUEST_DATA = "QuestionnaireActivity.Request.Data"
        const val ACTIVITY_REQUEST_CODE = 113
        fun getIntent(response: QuestionnaireResponse, startPosition: Int = 0) =
                Intent(App.getContext(), QuestionnaireActivity::class.java).apply {
                    putExtra(RESPONSE_DATA, response)
                    putExtra(CURRENT_QUESTION_POSITION, startPosition)
                }
    }

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mResponse: QuestionnaireResponse? = null

    private val mQuestionNavigator by lazy {
        QuestionScreenNavigator(mResponse?.questions ?: arrayOf<QuestionSettings>(), questionNavigator = this)
    }

    private val mAppConfig by lazy {
        App.getAppConfig()
    }

    private val mViewModel by lazy {
        QuestionnaireViewModel()
    }

    private var mQuestionaireSubscription: Subscription? = null
    private var mRequestData = mAppConfig.questionnaireAnswers
    private var mQuestionStartPosition: Int? = null
    private val mBackPressedOnce = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fillData(savedInstanceState)
        mQuestionaireSubscription = mEventBus
                .getObservable(UserChooseAnswer::class.java)
                .subscribe(shortSubscription {
                    mQuestionNavigator.show()
                    it?.json?.let { json ->
                        mRequestData.apply {
                            json.keys().forEach {
                                put(it, json.get(it))
                            }
                        }
                    }
                })
        // запускаем показы
        mQuestionNavigator.show(mQuestionStartPosition)
        viewBinding.viewModel = mViewModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GpPurchaseActivity.ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            finishSuccessfully()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mQuestionaireSubscription.safeUnsubscribe()
        mViewModel.release()
    }

    override fun addQuestionScreen(fragment: Fragment?) =
            fragment?.let {
                saveSettings()
                mViewModel.setCounterTitle(mQuestionNavigator.getCurrentPosition() + 1, mQuestionNavigator.getTotalPOsition())
                supportFragmentManager.beginTransaction().replace(R.id.content, fragment, null).commit()
                Unit
            } ?: Unit

    override fun showResultScreen() {
        mViewModel.visibility.set(View.GONE)
        mResponse?.let {
            saveSettings()
            val fragment = QuestionnaireResultFragment.newInstance(it.questionnaireMethodName, mRequestData)
            supportFragmentManager.beginTransaction().replace(R.id.content, fragment, null).commit()
        }
    }

    private fun saveSettings() {
        mAppConfig.currentQuestionPosition = mQuestionNavigator.getCurrentPosition()
        mAppConfig.questionnaireAnswers = mRequestData
        mAppConfig.saveConfig()
    }

    private fun finishSuccessfully() {
        // если пользователь прошел все круги ада с опросником, то дропаем счетчик, чтобы он больше не запустился
        mAppConfig.currentQuestionPosition = Integer.MIN_VALUE
        mAppConfig.questionnaireData = QuestionnaireResponse()
        mAppConfig.saveConfig()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun getToolbarBinding(binding: AcQuestionnaireBinding): ToolbarBinding = binding.toolbarInclude

    override fun getLayout() = R.layout.ac_questionnaire

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) = InvisibleToolbarViewModel(toolbar)

    override fun onUpButtonClick() {
        //ничего не делаем
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            it.putInt(CURRENT_QUESTION_POSITION, mQuestionNavigator.getCurrentPosition())
            it.putParcelable(RESPONSE_DATA, mResponse)
            it.putString(REQUEST_DATA, mRequestData.toString())
        }
    }

    private fun fillData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            if (it.containsKey(CURRENT_QUESTION_POSITION)) {
                mQuestionStartPosition = it.getInt(CURRENT_QUESTION_POSITION)
            }
            if (it.containsKey(RESPONSE_DATA)) {
                mResponse = it.getParcelable(RESPONSE_DATA)
            }
            if (it.containsKey(REQUEST_DATA)) {
                try {
                    mRequestData = JSONObject(it.getString(REQUEST_DATA))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        if (mQuestionStartPosition == null) {
            mQuestionStartPosition = intent.getIntExtra(CURRENT_QUESTION_POSITION, 0)
        }
        if (mResponse == null) {
            mResponse = intent.getParcelableExtra<QuestionnaireResponse>(RESPONSE_DATA)
        }
    }

    override fun onBackPressed() {
        if (!mBackPressedOnce.get()) {
            mBackPressedOnce.set(true)
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    mBackPressedOnce.set(false)
                }
            }, NavigationActivity.EXIT_TIMEOUT.toLong())
            R.string.press_back_more_to_close_app.showShortToast()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}