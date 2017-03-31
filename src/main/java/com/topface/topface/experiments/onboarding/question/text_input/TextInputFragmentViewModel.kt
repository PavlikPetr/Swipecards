package com.topface.topface.experiments.onboarding.question.text_input

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.InputValueSettings
import com.topface.topface.experiments.onboarding.question.UserChooseAnswer
import com.topface.topface.experiments.onboarding.question.digit_input.DigitInputFragment
import com.topface.topface.experiments.onboarding.question.text_input.TextInputFragment.Companion.EXTRA_DATA
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import org.json.JSONObject

/**
 * Вьюмодель экрана пятого типа вопроса в опроснике
 * Created by petrp on 30.03.2017.
 */
class TextInputFragmentViewModel(bundle: Bundle) : ILifeCycle {

    companion object {
        private const val MAX_LENGTH_DEFAULT = 1024
        private const val TITLE = "TextInputFragmentViewModel.Title"
        private const val ERROR = "TextInputFragmentViewModel.Error"
        private const val HINT = "TextInputFragmentViewModel.Hint"
        private const val IS_HINT_ENABLED = "TextInputFragmentViewModel.IsHintEnabled"
        private const val MAX_LENGTH = "TextInputFragmentViewModel.MaxLength"
        private const val TEXT = "TextInputFragmentViewModel.Text"
        private const val IS_ERROR_ENABLED = "TextInputFragmentViewModel.IsErrorEnabled"
        private const val UNIT = "TextInputFragmentViewModel.Unit"
        private const val IS_COUNTER_ENABLE = "TextInputFragmentViewModel.IsCounterEnable"
    }

    private var mData: InputValueSettings? = bundle.getParcelable(EXTRA_DATA)

    val title = ObservableField<String>(mData?.title ?: Utils.EMPTY)
    val error = ObservableField<String>()
    val hint = ObservableField<String>(mData?.hint ?: Utils.EMPTY)
    val isHintEnabled = ObservableBoolean(mData?.let { !it.hint.isNullOrEmpty() } ?: false)
    val maxLength = ObservableInt(mData?.max?.value ?: MAX_LENGTH_DEFAULT)
    val text = ObservableField<String>()
    val isErrorEnabled = ObservableBoolean(mData?.let { !it.max.errorMessage.isNullOrEmpty() && !it.min.errorMessage.isNullOrEmpty() } ?: false)
    val unit = ObservableField<String>(mData?.unit ?: Utils.EMPTY)
    val isCounterEnable = ObservableBoolean(mData?.let { it.max.value > 0 } ?: false)

    fun onNext() =
            with(text.get()) {
                this?.let {
                    if (it.length < mData?.min?.value ?: 0) {
                        error.set(mData?.min?.errorMessage ?: Utils.EMPTY)
                    } else if (it.length > mData?.max?.value ?: Int.MAX_VALUE) {
                        error.set(mData?.max?.errorMessage ?: Utils.EMPTY)
                    } else {
                        App.getAppComponent().eventBus().setData(UserChooseAnswer(JSONObject().apply {
                            mData?.fieldName?.let {
                                if (it.isNotEmpty()) {
                                    put(it, this@with)
                                }
                            }
                        }))
                    }
                }
            }

    fun release() {

    }

    override fun onSavedInstanceState(state: Bundle) {
        super.onSavedInstanceState(state)
        with(state) {
            putParcelable(DigitInputFragment.EXTRA_DATA, mData)
            putString(TITLE, title.get())
            putString(ERROR, error.get())
            putString(HINT, hint.get())
            putBoolean(IS_HINT_ENABLED, isHintEnabled.get())
            putInt(MAX_LENGTH, maxLength.get())
            putString(TEXT, text.get())
            putBoolean(IS_ERROR_ENABLED, isErrorEnabled.get())
            putString(UNIT, unit.get())
            putBoolean(IS_COUNTER_ENABLE, isCounterEnable.get())
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        with(state) {
            mData = getParcelable(TextInputFragment.EXTRA_DATA)
            title.set(getString(TITLE))
            error.set(getString(ERROR))
            hint.set(getString(HINT))
            isHintEnabled.set(getBoolean(IS_HINT_ENABLED))
            maxLength.set(getInt(MAX_LENGTH))
            text.set(getString(TEXT))
            isErrorEnabled.set(getBoolean(IS_ERROR_ENABLED))
            unit.set(getString(UNIT))
            isCounterEnable.set(getBoolean(IS_COUNTER_ENABLE))
        }
    }
}
