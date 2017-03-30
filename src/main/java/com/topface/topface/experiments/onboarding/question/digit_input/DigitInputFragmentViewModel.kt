package com.topface.topface.experiments.onboarding.question.digit_input

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.experiments.onboarding.question.InputValueSettings
import com.topface.topface.experiments.onboarding.question.UserChooseAnswer
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
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
class DigitInputFragmentViewModel(bundle: Bundle) {

    private val mData: InputValueSettings? = bundle.getParcelable(DigitInputFragment.EXTRA_DATA)

    val title = ObservableField<String>(mData?.title ?: Utils.EMPTY)
    val error = ObservableField<String>()
    val hint = ObservableField<String>(mData?.hint ?: Utils.EMPTY)
    val maxLength = ObservableInt(mData?.max?.value?.toString()?.length ?: 3)
    val text = RxFieldObservable<String>()
    val isErrorEnabled = ObservableBoolean(mData?.let { !it.max.errorMessage.isNullOrEmpty() && !it.min.errorMessage.isNullOrEmpty() } ?: false)
    val unit = ObservableField<String>(mData?.unit ?: Utils.EMPTY)

    private val mTextChangeSubscription: Subscription

    init {
        mTextChangeSubscription = text.filedObservable
                .subscribe(shortSubscription {
                    it?.let {
                        if (it.isNotEmpty() && it.safeToInt(kotlin.Int.MIN_VALUE) == Int.MIN_VALUE) {
                            error.set(R.string.general_wrong_field_value.getString())
                        } else {
                            error.set(Utils.EMPTY)
                        }
                        if (it.length == maxLength.get()) {
                            isValid(it.toInt())
                        }
                    }
                })
    }

    fun onNext() =
            with(text.get()) {
                if (this.safeToInt(kotlin.Int.MIN_VALUE) == Int.MIN_VALUE) {
                    error.set(R.string.general_wrong_field_value.getString())
                } else if (isValid(this.toInt())) {
                    App.getAppComponent().eventBus().setData(UserChooseAnswer(JSONObject().apply {
                        mData?.let {
                            put(it.fieldName, this@with.toInt())
                        }
                    }))
                }
            }

    private fun isValid(value: Int) = if (value < mData?.min?.value ?: 0) {
        error.set(mData?.min?.errorMessage ?: Utils.EMPTY)
        false
    } else if (value > mData?.max?.value ?: 0) {
        error.set(mData?.max?.errorMessage ?: Utils.EMPTY)
        false
    } else true

    fun release() {
        mTextChangeSubscription.safeUnsubscribe()
    }
}
