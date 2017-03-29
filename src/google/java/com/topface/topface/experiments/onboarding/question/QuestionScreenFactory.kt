package com.topface.topface.experiments.onboarding.question

import android.support.v4.app.Fragment
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.EnterTextScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.EnterValueScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.MultiSelectScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.RangeQuestionScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.SingleChoiseScreen
import com.topface.topface.experiments.onboarding.question.valueSetter.EnterValueFragment

/**
 * Фабрика для экранов опросника
 * Created by petrp on 29.03.2017.
 */
class QuestionScreenFactory(private val questions: Array<QuestionSettings>, private var startPosition: Int = -1,
                            private val questionNavigator: IQuestionNavigator) {
    fun show(position: Int = startPosition + 1) {
        questions.getOrNull(position)?.let { questionNavigator.addQuestionScreen(getFragmentByType(it)) } ?: questionNavigator.showResultScreen()
    }

    private fun getFragmentByType(data: QuestionSettings) =
            when (data.type) {
                RangeQuestionScreen -> EnterValueFragment.newInstance(data.typeThird)
                SingleChoiseScreen -> EnterValueFragment.newInstance(data.typeThird)
                EnterValueScreen -> EnterValueFragment.newInstance(data.typeThird)
                MultiSelectScreen -> EnterValueFragment.newInstance(data.typeThird)
                EnterTextScreen -> EnterValueFragment.newInstance(data.typeThird)
                else -> null
            }
}