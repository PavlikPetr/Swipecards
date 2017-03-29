package com.topface.topface.experiments.onboarding.question.valueSetter

import android.os.Bundle
import com.topface.topface.experiments.onboarding.question.QuestionTypeThird

/**
 * Вьюмодель экрана третьего типа вопроса в опроснике
 * Created by petrp on 30.03.2017.
 */
class EnterValueFragmentViewModel(bundle: Bundle) {
    private val mData: QuestionTypeThird? = bundle.getParcelable(EnterValueFragment.EXTRA_DATA)
}
