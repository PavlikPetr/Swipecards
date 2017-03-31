package com.topface.topface.experiments.onboarding.question

import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.EnterTextScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.EnterValueScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.MultiSelectScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.RangeQuestionScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.SingleChoiseScreen
import com.topface.topface.experiments.onboarding.question.multiselectCheckboxList.MultiSelectCheckboxListFragment
import com.topface.topface.experiments.onboarding.question.digit_input.DigitInputFragment
import com.topface.topface.experiments.onboarding.question.range.QRangeFragment
import com.topface.topface.experiments.onboarding.question.single_list.QSingleListFragment
import com.topface.topface.experiments.onboarding.question.text_input.TextInputFragment

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
                RangeQuestionScreen -> QRangeFragment.newInstance(data.typeFirst)
                SingleChoiseScreen -> QSingleListFragment.newInstance(data.typeSecond)
                EnterValueScreen -> DigitInputFragment.newInstance(data.questionWithInput)
                MultiSelectScreen -> MultiSelectCheckboxListFragment.newInstance(data.typeFourth)
                EnterTextScreen -> TextInputFragment.newInstance(data.questionWithInput)
                else -> null
            }
}