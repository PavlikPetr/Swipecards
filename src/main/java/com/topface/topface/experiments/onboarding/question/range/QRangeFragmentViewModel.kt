package com.topface.topface.experiments.onboarding.question.range

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.QuestionTypeFirst
import com.topface.topface.experiments.onboarding.question.UserChooseAnswer
import com.topface.topface.ui.views.RangeSeekBar
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import org.json.JSONObject

/**
 * VM for question about range
 * Created by m.bayutin on 28.03.17.
 */
class QRangeFragmentViewModel(bundle: Bundle) : RangeSeekBar.OnRangeSeekBarChangeListener<Int>, ILifeCycle {
    companion object {
        private const val STATE_MIN = "QRangeFragment.State.Min"
        private const val STATE_MAX = "QRangeFragment.State.Max"
        private const val STATE_END = "QRangeFragment.State.End"
        private const val STATE_START = "QRangeFragment.State.Start"
        private const val STATE_TITLE = "QRangeFragment.State.Title"
    }

    private var mData: QuestionTypeFirst? = bundle.getParcelable(QRangeFragment.EXTRA_DATA)

    val title = ObservableField<String>(mData?.title ?: Utils.EMPTY)
    val maxValue = object : ObservableInt(mData?.max?.value ?: 0) {
        override fun set(value: Int) {
            super.set(value)
            maxValueTitle.set(value.toString())
        }
    }

    val minValue = object : ObservableInt(mData?.min?.value ?: 0) {
        override fun set(value: Int) {
            super.set(value)
            minValueTitle.set(value.toString())
        }
    }

    val maxValueTitle = ObservableField<String>()
    val minValueTitle = ObservableField<String>()

    val end = ObservableInt(mData?.endValue ?: 0)
    val start = ObservableInt(mData?.startValue ?: 0)

    override fun onRangeSeekBarValuesChanged(bar: RangeSeekBar<*>?, minValue: Int, maxValue: Int, thumbId: RangeSeekBar.Thumb?) {
        if (thumbId != null) {
            when (thumbId) {
                RangeSeekBar.Thumb.MAX -> end.set(maxValue)
                RangeSeekBar.Thumb.MIN -> start.set(minValue)
            }
        } else {
            end.set(maxValue)
            start.set(minValue)
        }
    }

    fun onNext() {
        App.getAppComponent().eventBus().setData(UserChooseAnswer(JSONObject().apply {
            mData?.let {
                it.min?.let { put(it.fieldName, minValueTitle.get()) }
                it.max?.let { put(it.fieldName, maxValueTitle.get()) }
            }
        }))
    }

    override fun onSavedInstanceState(state: Bundle) {
        super.onSavedInstanceState(state)
        with(state) {
            putParcelable(QRangeFragment.EXTRA_DATA, mData)
            putInt(STATE_END, end.get())
            putInt(STATE_START, start.get())
            putInt(STATE_MIN, minValue.get())
            putInt(STATE_MAX, maxValue.get())
            putString(STATE_TITLE, title.get())
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        with(state) {
            mData = getParcelable(QRangeFragment.EXTRA_DATA)
            end.set(getInt(STATE_END))
            start.set(getInt(STATE_START))
            minValue.set(getInt(STATE_MIN))
            maxValue.set(getInt(STATE_MAX))
            title.set(getString(STATE_TITLE))
        }
    }
}