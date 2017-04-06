package com.topface.topface.experiments.onboarding.question.digit_input

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.experiments.onboarding.getDigitInputError
import com.topface.topface.experiments.onboarding.question.InputValueSettings
import com.topface.topface.experiments.onboarding.question.UserChooseAnswer
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.safeToInt
import com.topface.topface.utils.rx.RxFieldObservable
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONObject
import rx.Subscription

/**
 * Вьюмодель экрана третьего типа вопроса в опроснике
 * Created by petrp on 30.03.2017.
 */
class DigitInputFragmentViewModel(bundle: Bundle, private val keyboard: IKeyboard) : ILifeCycle {

    companion object {
        private const val TITLE = "DigitInputFragmentViewModel.Title"
        private const val ERROR = "DigitInputFragmentViewModel.Error"
        private const val HINT = "DigitInputFragmentViewModel.Hint"
        private const val MAX_LENGTH = "DigitInputFragmentViewModel.MaxLength"
        private const val TEXT = "DigitInputFragmentViewModel.Text"
        private const val IS_ERROR_ENABLED = "DigitInputFragmentViewModel.IsErrorEnabled"
        private const val UNIT = "DigitInputFragmentViewModel.Unit"
        private const val MIN_WIDTH = "DigitInputFragmentViewModel.MinWidth"
    }

    private var mData: InputValueSettings? = bundle.getParcelable(DigitInputFragment.EXTRA_DATA)

    val title = ObservableField<String>(mData?.title ?: Utils.EMPTY)
    val error = ObservableField<String>()
    val hint = ObservableField<String>(mData?.hint ?: Utils.EMPTY)
    val maxLength = ObservableInt(mData?.max?.value?.toString()?.length ?: 3)
    val text = RxFieldObservable<String>()
    val isErrorEnabled = ObservableBoolean(mData?.let { !it.max.errorMessage.isNullOrEmpty() && !it.min.errorMessage.isNullOrEmpty() } ?: false)
    val unit = ObservableField<String>(mData?.unit ?: Utils.EMPTY)
    val minWidth = ObservableInt(R.dimen.questionnaire_digit_input_one_sign_width.getDimen().toInt() * maxLength.get())

    val onEditorActionListener = TextView.OnEditorActionListener { v, actionId, event ->
        if (event?.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
            onNext()
            true
        } else false
    }

    private val mTextChangeSubscription: Subscription

    init {
        mTextChangeSubscription = text.filedObservable
                .subscribe(shortSubscription {
                    it?.let { value ->
                        mData?.let {
                            error.set(value.getDigitInputError(it.min, it.max).second)
                        } ?: error.set(Utils.EMPTY)
                    }
                })
    }

    fun onNext() =
            mData?.let {
                val value = text.get()
                value.getDigitInputError(it.min, it.max).run {
                    if (first) {
                        error.set(second)
                    } else {
                        App.getAppComponent().eventBus().setData(UserChooseAnswer(JSONObject().apply {
                            mData?.fieldName?.let {
                                if (it.isNotEmpty()) {
                                    put(it, value.safeToInt())
                                }
                            }
                        }))
                        keyboard.hideKeyboard()
                    }
                }
            }

    fun release() = mTextChangeSubscription.safeUnsubscribe()

    override fun onSavedInstanceState(state: Bundle) {
        super.onSavedInstanceState(state)
        with(state) {
            putParcelable(DigitInputFragment.EXTRA_DATA, mData)
            putString(TITLE, title.get())
            putString(ERROR, error.get())
            putString(HINT, hint.get())
            putInt(MAX_LENGTH, maxLength.get())
            putString(TEXT, text.get())
            putBoolean(IS_ERROR_ENABLED, isErrorEnabled.get())
            putString(UNIT, unit.get())
            putInt(MIN_WIDTH, minWidth.get())
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        with(state) {
            mData = getParcelable(DigitInputFragment.EXTRA_DATA)
            title.set(getString(TITLE))
            error.set(getString(ERROR))
            hint.set(getString(HINT))
            maxLength.set(getInt(MAX_LENGTH))
            text.set(getString(TEXT))
            isErrorEnabled.set(getBoolean(IS_ERROR_ENABLED))
            unit.set(getString(UNIT))
            minWidth.set(getInt(MIN_WIDTH))
        }

    }

}
