package com.topface.topface.experiments.onboarding.question.range

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.ui.views.RangeSeekBar

/**
 * VM for question about range
 * Created by m.bayutin on 28.03.17.
 */
class QRangeFragmentViewModel : RangeSeekBar.OnRangeSeekBarChangeListener<Int> {
    val maxValue = object : ObservableInt(300) {
        override fun set(value: Int) {
            super.set(value)
            maxValueTitle.set(value.toString())
        }
    }
    val minValue = object : ObservableInt(0) {
        override fun set(value: Int) {
            super.set(value)
            minValueTitle.set(value.toString())
        }
    }
    val maxValueTitle = ObservableField<String>()
    val minValueTitle = ObservableField<String>()

    val end = ObservableInt()
    val start = ObservableInt()

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
}