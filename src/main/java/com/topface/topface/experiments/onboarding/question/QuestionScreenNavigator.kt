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
class QuestionScreenNavigator(private val questions: Array<QuestionSettings>, private var startPosition: Int = -1,
                              private val questionNavigator: IQuestionNavigator) {
    fun show(position: Int? = null) {
        val pos = position ?: startPosition + 1
        startPosition = pos
        questions.getOrNull(pos)?.let { questionNavigator.addQuestionScreen(getFragmentByType(it)) } ?: questionNavigator.showResultScreen()
    }

    fun getCurrentPosition() = startPosition

    fun getTotalPOsition() = questions.size

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