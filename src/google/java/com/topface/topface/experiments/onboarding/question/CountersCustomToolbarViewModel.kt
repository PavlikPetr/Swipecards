package com.topface.topface.experiments.onboarding.question

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import kotlin.properties.Delegates

/**
 * Created by ppavlik on 07.11.16.
 * Вьюмоделька для кастомной вью в тулбаре покупок
 */

class CountersCustomToolbarViewModel {
    companion object {
        private const val COUNTER_TEMPLATE = "%s/%s"
    }

    val title = ObservableField<String>()
    val visibility = ObservableInt(View.GONE)

    var currentPosition by Delegates.observable(0) { prop, old, new ->
        if (new != old) {
            if (new <= 0) {
                visibility.set(View.GONE)
            } else {
                visibility.set(View.VISIBLE)
                setCounterTitle()
            }
        }
    }

    var questionsCount by Delegates.observable(0) { prop, old, new ->
        if (new != old) {
            if (new <= 0) {
                visibility.set(View.GONE)
            } else {
                visibility.set(View.VISIBLE)
                setCounterTitle()
            }
        }
    }

    private fun setCounterTitle() = title.set(String.format(COUNTER_TEMPLATE, currentPosition, questionsCount))

    fun release() {

    }
}