package com.topface.topface.experiments.onboarding.question

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.R
import com.topface.topface.utils.extensions.getString
import kotlin.properties.Delegates

/**
 * Created by ppavlik on 07.11.16.
 * Вьюмоделька для кастомной вью в тулбаре покупок
 */

class CountersCustomToolbarViewModel {

    val title = ObservableField<String>()
    val visibility = ObservableInt(View.GONE)

    private var currentPosition: Int = 0
    private var questionsCount: Int = 0

    fun setCounterTitle(currentPosition: Int = this.currentPosition, questionsCount: Int = this.questionsCount) {
        this.currentPosition = currentPosition
        this.questionsCount = questionsCount
        if (currentPosition <= 0 || questionsCount <= 0) {
            visibility.set(View.GONE)
        } else {
            visibility.set(View.VISIBLE)
            title.set(String.format(R.string.num_from_num.getString(), currentPosition, questionsCount))
        }
    }

    fun release() {

    }
}